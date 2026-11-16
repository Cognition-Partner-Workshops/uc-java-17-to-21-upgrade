package io.spring.application.profile;

/** The closed set of activities the profile summary builds and knows how to describe. */
public sealed interface SummaryActivity extends ProfileActivity
    permits AuthoredArticle, FollowerTally, FavoriteTally {}
