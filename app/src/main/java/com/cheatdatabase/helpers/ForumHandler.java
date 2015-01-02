package com.cheatdatabase.helpers;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cheatdatabase.businessobjects.ForumPost;

public class ForumHandler extends DefaultHandler {

	private ForumPost singleForumPost = new ForumPost();
	private ArrayList<ForumPost> forumPosts = new ArrayList<ForumPost>();

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public ForumPost getParsedData() {
		return this.singleForumPost;
	}

	public ArrayList<ForumPost> getForumPosts() {
		return this.forumPosts;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		this.singleForumPost = new ForumPost();
		this.forumPosts = new ArrayList<ForumPost>();
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
	}

	/**
	 * Gets be called on opening tags like: <tag> Can provide attribute(s), when
	 * xml was like: <tag attribute="attributeValue">
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (localName.equals("resources")) {
		} else if (localName.equals("forumpost")) {
			// Extract an Attribute
			String idString = atts.getValue("forum_post_id");
			int forumPostId = Integer.parseInt(idString);

			String memberIdString = atts.getValue("member_id");
			int memberId = Integer.parseInt(memberIdString);

			String name = atts.getValue("name");
			String username = atts.getValue("username");
			String email = atts.getValue("email");
			String text = atts.getValue("text");
			String created = atts.getValue("created");
			String updated = atts.getValue("updated");
			String ip = atts.getValue("ip");

			ForumPost sfp = new ForumPost();
			sfp.setCheatId(forumPostId);
			sfp.setMemberId(memberId);
			sfp.setName(name);
			sfp.setUsername(username);
			sfp.setEmail(email);
			sfp.setText(text);
			sfp.setCreated(created);
			sfp.setUpdated(updated);
			sfp.setIp(ip);

			forumPosts.add(sfp);
		}
	}

	/**
	 * Gets be called on closing tags like: </tag>
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if (localName.equals("resources")) {
		} else if (localName.equals("forumpost")) {
		}
	}

	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char ch[], int start, int length) {
	}

}
