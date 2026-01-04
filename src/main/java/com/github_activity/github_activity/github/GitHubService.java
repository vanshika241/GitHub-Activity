package com.github_activity.github_activity.github;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;
import javax.print.attribute.standard.Media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.core.joran.action.Action;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class GitHubService {
    
    private final RestTemplate rest;
    private final ObjectMapper mapper;
    String token = System.getenv("GITHUB_TOKEN");

    @Value("${github.token:}")
    private String githubToken;
    public GitHubService(){
        this.rest = new RestTemplate();
        this.mapper = new ObjectMapper();

    }
    
    private HttpEntity<String> authEntity(){
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization" , token);
      headers.set("Accept", "application/vnd.github+json");
      headers.setAccept(List.of(MediaType.APPLICATION_JSON));
      if (githubToken != null && !githubToken.isBlank()) {
          headers.setBearerAuth(githubToken);
      }
      return new HttpEntity<>(headers);
    }

    public GitHubUser getUserProfile(String username){
        String url = "https://api.github.com/users/"+username;
        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET , authEntity() , String.class);
        try{
            return mapper.readValue(resp.getBody(), GitHubUser.class);
        }catch(Exception e){
            throw new RuntimeException("Failed to parse user profile" , e);
        }
    }

    public List<ActivityItem> getRecentActivities(String username , int limit){
String url = "https://api.github.com/users/" + username + "/events/public";
ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, authEntity(), String.class);
        try {
            JsonNode root = mapper.readTree(resp.getBody());
            List<ActivityItem> out = new ArrayList<>();
            int cnt = 0;
            for(JsonNode event : root){
                if(cnt>=limit){
                    break;
                }
                ActivityItem it = new ActivityItem();
                it.setType(event.path("type").asText());
                it.setRepoName(event.path("repo").path("name").asText());
               it.setCreatedAt(event.path("created_at").asText());

                if (it.getType().equals("PushEvent")) {
                    int commits = event.path("payload").path("commits").size();
                    it.setSummary("Pushed " + commits + " commit(s)");
                } else if (it.getType().contains("PullRequest")) {
                    it.setSummary(event.path("payload").path("action").asText() + " pull request");
                } else {
                    it.setSummary(event.path("payload").toString());
                }
                out.add(it);
                cnt++;
            }
            return out;
        } catch (Exception e) {
           throw new RuntimeException("Failed to parse Events",e);
        }
    }

        public int getTotalCommitsForUser(String username) {
        // 1) get repos
        String reposUrl = "https://api.github.com/users/" + username + "/repos?per_page=100";
        ResponseEntity<String> resp = rest.exchange(reposUrl, HttpMethod.GET, authEntity(), String.class);
        try {
            JsonNode repos = mapper.readTree(resp.getBody());
            int total = 0;
            for (JsonNode repo : repos) {
                String repoName = repo.path("name").asText();
                String owner = repo.path("owner").path("login").asText();
                String contribUrl = "https://api.github.com/repos/" + owner + "/" + repoName + "/contributors?per_page=100";
                try {
                    ResponseEntity<String> cResp = rest.exchange(contribUrl, HttpMethod.GET, authEntity(), String.class);
                    JsonNode contributors = mapper.readTree(cResp.getBody());
                    for (JsonNode c : contributors) {
                        if (username.equalsIgnoreCase(c.path("login").asText())) {
                            total += c.path("contributions").asInt();
                            break;
                        }
                    }
                } catch (Exception ex) {
                    // skip repo on error (private repo or rate limit)
                }
            }
            return total;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch repos", e);
        }
    }

}
