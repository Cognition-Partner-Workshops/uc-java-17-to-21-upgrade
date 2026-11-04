package io.spring.infrastructure.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.spring.application.profile.AuthoredArticle;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.mybatis.readservice.ProfileSummaryReadService;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
  MyBatisUserRepository.class,
  MyBatisArticleRepository.class,
  MyBatisArticleFavoriteRepository.class
})
public class ProfileSummaryReadServiceTest extends DbTestBase {
  @Autowired private ProfileSummaryReadService profileSummaryReadService;
  @Autowired private UserRepository userRepository;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  @Test
  public void should_read_authored_articles_followers_and_favorites() {
    User author = new User("author@test.com", "author", "123", "", "");
    User reader = new User("reader@test.com", "reader", "123", "", "");
    userRepository.save(author);
    userRepository.save(reader);
    Article newer =
        new Article("Newer", "d", "b", Arrays.asList("java"), author.getId(), new DateTime(2000L));
    Article older =
        new Article("Older", "d", "b", Arrays.asList("java"), author.getId(), new DateTime(1000L));
    articleRepository.save(newer);
    articleRepository.save(older);
    articleFavoriteRepository.save(new ArticleFavorite(older.getId(), reader.getId()));
    userRepository.saveRelation(new FollowRelation(reader.getId(), author.getId()));

    List<AuthoredArticle> articles = profileSummaryReadService.authoredArticles(author.getId());

    assertEquals(2, articles.size());
    assertEquals(new AuthoredArticle("older", "Older", 1), articles.get(0));
    assertEquals(new AuthoredArticle("newer", "Newer", 0), articles.get(1));
    assertEquals(1, profileSummaryReadService.countFollowers(author.getId()));
    assertEquals(0, profileSummaryReadService.countFollowers(reader.getId()));
    assertEquals(1, profileSummaryReadService.countFavoritesGiven(reader.getId()));
  }
}
