/**
 * 
 */
package com.bdoku.model;

/**
 * @author bwinters
 * 
 */
public class BoardPostObject {

    private String title;
    private String caption;
    private String description;
    private String link;
    private String imageLink;

    public BoardPostObject(String title, String caption, String description, String link, String imageLink) {
	this.title = title;
	this.caption = caption;
	this.description = description;
	this.link = link;
	this.imageLink = imageLink;
    }

    public String toString() {
	return getTitle();
    }

    public String getTitle() {
	return title;
    }

    public String getCaption() {
	return caption;
    }

    public String getDescription() {
	return description;
    }

    public String getLink() {
	return link;
    }

    public String getImageLink() {
	return imageLink;
    }
}
