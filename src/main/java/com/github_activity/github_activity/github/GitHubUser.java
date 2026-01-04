package com.github_activity.github_activity.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubUser {

    private String login;
    private String name;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("public_repos")
    private int publicRepos;

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getPublicRepos() {
        return publicRepos;
    }
    public void setPublicRepos(int publicRepos) {
        this.publicRepos = publicRepos;
    }
}
