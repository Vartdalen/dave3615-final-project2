package no.oslomet.gatewayservice.controller;

import no.oslomet.gatewayservice.model.Tweet;
import no.oslomet.gatewayservice.model.User;
import no.oslomet.gatewayservice.service.TweetService;
import no.oslomet.gatewayservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;
    @Autowired
    private TweetService tweetService;

    @GetMapping("/")
    public String home(Model model){
        List<Tweet> tweetList = tweetService.getAllTweets();

        for(Tweet tweet: tweetList) {
            System.out.println(tweet.toString());
        }

        setUserModel(model, SecurityContextHolder.getContext().getAuthentication(), userService);
        model.addAttribute("tweetList", tweetList);
        return "index";
    }

    private void setUserModel(Model model, Authentication auth, UserService userService) {
        Optional<User> user = userService.getUserByEmail(auth.getName());
        if(user.isPresent()) {
            model.addAttribute("user", user.get());
        }
    }
}
