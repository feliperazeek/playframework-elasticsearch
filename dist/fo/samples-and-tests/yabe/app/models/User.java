package models;
 
import javax.persistence.Entity;

import play.Logger;
import play.db.jpa.Model;
import play.mvc.Scope.Session;
 
@Entity
public class User extends Model {
 
    public String email;

    public String password;
    
    public String fullname;
    
    public boolean isAdmin;
    
    public String industry;
    
    public String headline;
    
    public String linkedInToken;
    
    public String linkedInId;
    
    public String pictureUrl;
    
    public User() {}
    
    public User(String email, String password, String fullname,
			boolean isAdmin, String industry, String headline,
			String linkedInToken, String linkedInId, String pictureUrl) {
		super();
		this.email = email;
		this.password = password;
		this.fullname = fullname;
		this.isAdmin = isAdmin;
		this.industry = industry;
		this.headline = headline;
		this.linkedInToken = linkedInToken;
		this.linkedInId = linkedInId;
		this.pictureUrl = pictureUrl;
	}
    
    public User(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
    }
    
    public static User connect(String email, String password) {
        return find("byEmailAndPassword", email, password).first();
    }
    
    public String toString() {
        return email;
    }
    
    public static User findConnectedUser(String connected) {
    	String linkedInKey = "linkedin:";
    	if (connected != null && connected.startsWith(linkedInKey) ) {
    		User user = User.findByLinkedInId(connected.replaceAll(linkedInKey, ""));
    		return user;
    	} else {
    		User user = User.find("byEmail", connected).first();
            return user;
    	}
    }
    
    public static User findByLinkedInId(String linkedInId) {
    	return find("byLinkedInId", linkedInId).first();
    }
    
	public static void linkedinOAuthCallback(play.modules.linkedin.LinkedInProfile profile) {
		Logger.info("Handle LinkedIn OAuth Callback: " + profile);
		User user = findByLinkedInId(profile.getId());
		String username = "linkedin:" + profile.getId();
		if(user == null || user.linkedInToken == null) {
			user = new User();
			user.fullname = (new StringBuffer().append(profile.getFirstName()).append(" ").append(profile.getLastName())).toString();
			user.linkedInId = profile.getId();
			user.industry = profile.getIndustry();
			user.headline = profile.getHeadline();
			user.pictureUrl = profile.getPictureUrl();
			user.linkedInToken = profile.getAccessToken();
			user.isAdmin = true;
			user = user.save();
		} else {
			Logger.info("Found User: " + user);
			user.linkedInToken = profile.getAccessToken();
			user.save();
		}
		if ( user == null ) {
			throw new RuntimeException("Could not store or lookup user with LinkedIn Profile: " + profile);
		}
		Session.current().put("username", username);
	}
 
}