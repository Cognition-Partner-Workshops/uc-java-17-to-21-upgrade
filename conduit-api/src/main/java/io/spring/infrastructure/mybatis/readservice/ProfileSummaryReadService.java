package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.profile.AuthoredArticle;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileSummaryReadService {
  List<AuthoredArticle> authoredArticles(@Param("userId") String userId);

  int countFollowers(@Param("userId") String userId);

  int countFavoritesGiven(@Param("userId") String userId);
}
