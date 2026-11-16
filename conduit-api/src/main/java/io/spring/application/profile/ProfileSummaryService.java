package io.spring.application.profile;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.ProfileSummaryReadService;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Builds a profile summary by fanning out three blocking reads (articles, followers, favourites),
 * each on its own virtual thread. Each read carries a simulated remote-call delay; a semaphore caps
 * how many reads are in flight at once across all requests.
 */
@Service
public class ProfileSummaryService implements DisposableBean {
  private static final Logger log = LoggerFactory.getLogger(ProfileSummaryService.class);

  private final UserReadService userReadService;
  private final ProfileSummaryReadService profileSummaryReadService;
  private final ExecutorService executor;
  private final Semaphore readPermits;
  private final long simulatedLatencyMs;
  private final String publicBaseUrl;

  public ProfileSummaryService(
      UserReadService userReadService,
      ProfileSummaryReadService profileSummaryReadService,
      @Value("${conduit.profile-summary.max-concurrent-reads:256}") int maxConcurrentReads,
      @Value("${conduit.profile-summary.simulated-latency-ms:50}") long simulatedLatencyMs,
      @Value("${conduit.public-base-url:https://conduit.example.com}") String publicBaseUrl) {
    this.userReadService = userReadService;
    this.profileSummaryReadService = profileSummaryReadService;
    this.simulatedLatencyMs = simulatedLatencyMs;
    this.publicBaseUrl = publicBaseUrl;
    this.readPermits = new Semaphore(maxConcurrentReads);
    this.executor =
        Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("profile-summary-", 0).factory());
  }

  public Optional<ProfileSummaryData> summarize(String username) {
    UserData user = userReadService.findByUsername(username);
    if (user == null) {
      return Optional.empty();
    }
    String userId = user.getId();
    CompletableFuture<List<AuthoredArticle>> articlesFuture =
        fetch(() -> profileSummaryReadService.authoredArticles(userId));
    CompletableFuture<Integer> followersFuture =
        fetch(() -> profileSummaryReadService.countFollowers(userId));
    CompletableFuture<Integer> favoritesGivenFuture =
        fetch(() -> profileSummaryReadService.countFavoritesGiven(userId));

    List<AuthoredArticle> articles = articlesFuture.join();
    int followers = followersFuture.join();
    int favoritesGiven = favoritesGivenFuture.join();

    int favoritesReceived = articles.stream().mapToInt(AuthoredArticle::favoritesCount).sum();
    AuthoredArticle latestArticle = articles.isEmpty() ? null : articles.getLast();

    List<ProfileActivity> activities = new ArrayList<>(articles);
    activities.add(new FollowerTally(followers));
    activities.add(new FavoriteTally(favoritesGiven, favoritesReceived));
    List<String> activity = activities.stream().map(ActivityDescriber::describe).toList();

    return Optional.of(
        new ProfileSummaryData(
            user.getUsername(),
            articles.size(),
            followers,
            favoritesGiven,
            favoritesReceived,
            latestArticle,
            activity,
            profileUrl(user.getUsername())));
  }

  private <T> CompletableFuture<T> fetch(Supplier<T> call) {
    return CompletableFuture.supplyAsync(
        () -> {
          log.debug("profile summary read on thread {}", Thread.currentThread().threadId());
          acquireReadPermit();
          try {
            simulateRemoteCall();
            return call.get();
          } finally {
            readPermits.release();
          }
        },
        executor);
  }

  private void acquireReadPermit() {
    try {
      readPermits.acquire();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted waiting for a profile summary read", e);
    }
  }

  private void simulateRemoteCall() {
    if (simulatedLatencyMs <= 0) {
      return;
    }
    try {
      Thread.sleep(simulatedLatencyMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted during profile summary read", e);
    }
  }

  private String profileUrl(String username) {
    try {
      return URI.create(publicBaseUrl + "/profiles/" + username).toURL().toString();
    } catch (MalformedURLException | IllegalArgumentException e) {
      throw new IllegalStateException("Bad public base URL: " + publicBaseUrl, e);
    }
  }

  /** Stops accepting reads and waits for in-flight ones to finish. */
  @Override
  public void destroy() {
    executor.close();
  }
}
