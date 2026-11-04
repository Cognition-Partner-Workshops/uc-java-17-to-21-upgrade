"""Conduit parity scenarios. Run against a freshly started app (empty users created here, plus the
Flyway seed data). Scenarios run in order and share state (tokens, slugs) through `s`."""

PASSWORD = "parity-pass-123"


def user(name):
    return {"user": {"email": "%s@parity.test" % name, "username": name, "password": PASSWORD}}


def article(title, tags):
    return {"article": {"title": title, "description": "About " + title,
                        "body": "Body of " + title, "tagList": tags}}


def gql(c, query, variables=None, token=None):
    return c.call("POST", "/graphql", {"query": query, "variables": variables or {}}, token=token)


def register(c, s):
    for name in ["parity-alice", "parity-bob"]:
        _, body = c.call("POST", "/users", user(name))
        s[name] = body["user"]["token"]


def register_duplicate_422(c, s):
    c.call("POST", "/users", user("parity-alice"))
    c.call("POST", "/users", {"user": {"email": "not-an-email", "username": "", "password": ""}})


def login(c, s):
    _, body = c.call("POST", "/users/login",
                     {"user": {"email": "parity-alice@parity.test", "password": PASSWORD}})
    s["parity-alice"] = body["user"]["token"]
    c.call("POST", "/users/login", {"user": {"email": "parity-alice@parity.test", "password": "wrong"}})


def current_user(c, s):
    c.call("GET", "/user", token=s["parity-alice"])


def unauthorized_401(c, s):
    c.call("GET", "/user")
    c.call("GET", "/articles/feed")
    c.call("GET", "/user", token="not-a-real-token")


def update_user(c, s):
    c.call("PUT", "/user", {"user": {"bio": "Underwriter by day", "image": "https://img.test/a.png"}},
           token=s["parity-alice"])
    c.call("PUT", "/user", {"user": {"email": "parity-bob@parity.test"}}, token=s["parity-alice"])


def create_article(c, s):
    _, body = c.call("POST", "/articles", article("Parity Policy One", ["claims", "motor"]),
                     token=s["parity-alice"])
    s["slug1"] = body["article"]["slug"]
    c.call("POST", "/articles", {"article": {"title": "", "description": "", "body": ""}},
           token=s["parity-alice"])


def get_article(c, s):
    c.call("GET", "/articles/" + s["slug1"])
    c.call("GET", "/articles/" + s["slug1"], token=s["parity-bob"])


def update_article(c, s):
    _, body = c.call("PUT", "/articles/" + s["slug1"],
                     {"article": {"title": "Parity Policy Renewed", "body": "Updated body"}},
                     token=s["parity-alice"])
    s["slug1"] = body["article"]["slug"]
    c.call("PUT", "/articles/" + s["slug1"], {"article": {"body": "hijack"}}, token=s["parity-bob"])


def favorite(c, s):
    c.call("POST", "/articles/%s/favorite" % s["slug1"], token=s["parity-bob"])
    c.call("GET", "/articles?favorited=parity-bob")
    c.call("DELETE", "/articles/%s/favorite" % s["slug1"], token=s["parity-bob"])


def comment(c, s):
    _, body = c.call("POST", "/articles/%s/comments" % s["slug1"],
                     {"comment": {"body": "Looks good"}}, token=s["parity-bob"])
    comment_id = body["comment"]["id"]
    c.call("GET", "/articles/%s/comments" % s["slug1"])
    c.call("DELETE", "/articles/%s/comments/%s" % (s["slug1"], comment_id), token=s["parity-alice"])
    c.call("GET", "/articles/%s/comments" % s["slug1"])


def follow(c, s):
    c.call("POST", "/profiles/parity-alice/follow", token=s["parity-bob"])
    c.call("GET", "/profiles/parity-alice", token=s["parity-bob"])
    c.call("DELETE", "/profiles/parity-alice/follow", token=s["parity-bob"])
    c.call("GET", "/profiles/parity-alice", token=s["parity-bob"])


def feed_paging(c, s):
    c.call("POST", "/profiles/parity-alice/follow", token=s["parity-bob"])
    for n in ["Two", "Three", "Four"]:
        c.call("POST", "/articles", article("Parity Feed " + n, ["feed"]), token=s["parity-alice"])
    c.call("GET", "/articles/feed?limit=2&offset=0", token=s["parity-bob"])
    c.call("GET", "/articles/feed?limit=2&offset=2", token=s["parity-bob"])
    c.call("GET", "/articles/feed?limit=2&offset=4", token=s["parity-bob"])


def article_listing(c, s):
    c.call("GET", "/articles?author=parity-alice&limit=2")
    c.call("GET", "/articles?tag=feed")
    c.call("GET", "/articles?limit=3&offset=0")


def tags(c, s):
    c.call("GET", "/tags")


def delete_article(c, s):
    c.call("DELETE", "/articles/" + s["slug1"], token=s["parity-bob"])
    c.call("DELETE", "/articles/" + s["slug1"], token=s["parity-alice"])
    c.call("GET", "/articles/" + s["slug1"])


def not_found_404(c, s):
    c.call("GET", "/articles/no-such-article")
    c.call("GET", "/profiles/no-such-user")
    c.call("POST", "/articles/no-such-article/favorite", token=s["parity-bob"])


def profile_summary(c, s):
    c.call("GET", "/profiles/parity-alice/summary")
    c.call("GET", "/profiles/johndoe/summary")
    c.call("GET", "/profiles/no-such-user/summary")


def graphql_create_article(c, s):
    _, body = gql(c, """
        mutation($input: CreateArticleInput!) {
          createArticle(input: $input) {
            article { slug title description tagList favorited favoritesCount author { username } }
          }
        }""", {"input": {"title": "Parity GraphQL Article", "description": "Via GraphQL",
                         "body": "GraphQL body", "tagList": ["graphql"]}}, token=s["parity-alice"])
    s["gqlSlug"] = body["data"]["createArticle"]["article"]["slug"]


def graphql_query_article(c, s):
    gql(c, """
        query($slug: String!) {
          article(slug: $slug) { slug title body tagList createdAt author { username following } }
        }""", {"slug": s["gqlSlug"]})
    gql(c, """
        query {
          articles(first: 2, authoredBy: "parity-alice") {
            edges { node { slug title } }
            pageInfo { hasNextPage hasPreviousPage }
          }
        }""")


def graphql_me_and_profile(c, s):
    gql(c, "query { me { username email profile { bio image } } }", token=s["parity-alice"])
    gql(c, 'query { profile(username: "parity-alice") { profile { username bio following } } }',
        token=s["parity-bob"])
    gql(c, "query { me { username } }")


def graphql_errors(c, s):
    gql(c, """
        mutation {
          createUser(input: {email: "parity-alice@parity.test", username: "parity-alice",
                             password: "x"}) {
            ... on Error { message errors { key value } }
            ... on UserPayload { user { username } }
          }
        }""")
    gql(c, 'mutation { deleteArticle(slug: "no-such-article") { success } }', token=s["parity-bob"])


SCENARIOS = [
    ("register", register),
    ("register-validation-422", register_duplicate_422),
    ("login", login),
    ("current-user", current_user),
    ("unauthorized-401", unauthorized_401),
    ("update-user", update_user),
    ("create-article", create_article),
    ("get-article", get_article),
    ("update-article", update_article),
    ("favorite", favorite),
    ("comment", comment),
    ("follow", follow),
    ("feed-paging", feed_paging),
    ("article-listing", article_listing),
    ("tags", tags),
    ("delete-article", delete_article),
    ("not-found-404", not_found_404),
    ("profile-summary", profile_summary),
    ("graphql-create-article", graphql_create_article),
    ("graphql-query-article", graphql_query_article),
    ("graphql-me-and-profile", graphql_me_and_profile),
    ("graphql-errors", graphql_errors),
]
