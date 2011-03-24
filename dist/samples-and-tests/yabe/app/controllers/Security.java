package controllers;

import models.User;
import play.Logger;

public class Security extends Secure.Security {

    static boolean authentify(String username, String password) {
        return User.connect(username, password) != null;
    }
    
    static boolean check(String profile) {
    	Logger.info("Check Profile: %s", profile);
        if("admin".equals(profile)) {
            return User.findConnectedUser(connected()).isAdmin;
        }
        return false;
    }
    
    static void onDisconnected() {
        Application.index();
    }
    
    static void onAuthenticated() {
        Admin.index();
    }
    
}

