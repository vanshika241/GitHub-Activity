package com.github_activity.github_activity.github;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/github")
public class UserController {

    @Autowired
    private GitHubService githubService;

    @GetMapping("/{username}")
    public ResponseEntity<?> getGithubStats(@PathVariable String username) {
        try {
            GitHubUser profile = githubService.getUserProfile(username);
            List<ActivityItem> recent = githubService.getRecentActivities(username, 9);
            int totalCommits = githubService.getTotalCommitsForUser(username);
            Map<String, Object> out = new HashMap<>();
            out.put("profile", profile);
            out.put("recent", recent);
            out.put("totalCommits", totalCommits);
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(Map.of("error", e.getMessage()));
        }
    }
}
