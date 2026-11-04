package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.profile.ProfileSummaryService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "profiles/{username}/summary")
@AllArgsConstructor
public class ProfileSummaryApi {
  private ProfileSummaryService profileSummaryService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getSummary(@PathVariable("username") String username) {
    return profileSummaryService
        .summarize(username)
        .map(
            summary -> {
              Map<String, Object> body = new HashMap<>();
              body.put("summary", summary);
              return ResponseEntity.ok(body);
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}
