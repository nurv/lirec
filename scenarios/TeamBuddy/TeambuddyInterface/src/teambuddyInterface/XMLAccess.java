package teambuddyInterface;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class XMLAccess {

	private final static boolean WRITE_BAK = true;
	private final static String SUFFIX_BAK = ".bak";

	public UserData loadXML(String inputFilename) {
		UserData userData = null;
		try {
			DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(inputFilename));
			userData = readDocument(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userData;
	}

	private UserData readDocument(Document doc) {

		// create user data
		Node userDataNode = doc.getElementsByTagName("UserData").item(0);
		UserData userData = new UserData();

		// parse user data
		NamedNodeMap userDataAttributes = userDataNode.getAttributes();
		for (int i = 0; i < userDataAttributes.getLength(); i++) {
			Node userDataAttributeNode = userDataAttributes.item(i);
			if (userDataAttributeNode.getNodeName().equals("nextId")) {
				long nextId = Long.valueOf(userDataAttributeNode.getNodeValue());
				userData.setNextId(nextId);
			}
		}

		// parse user data children
		NodeList userDataChildNodes = userDataNode.getChildNodes();
		for (int i = 0; i < userDataChildNodes.getLength(); i++) {
			Node userDataChildNode = userDataChildNodes.item(i);

			// parse roles node
			if (userDataChildNode.getNodeName().equals("Roles")) {
				Node rolesNode = userDataChildNode;

				// parse roles node children
				NodeList rolesChildNodes = rolesNode.getChildNodes();
				for (int j = 0; j < rolesChildNodes.getLength(); j++) {
					Node rolesChildNode = rolesChildNodes.item(j);

					// parse roles
					if (rolesChildNode.getNodeName().equals("Role")) {

						// create role
						Node roleNode = rolesChildNode;
						Role role = new Role();
						userData.getRoles().add(role);

						// parse role attributes
						NamedNodeMap roleAttributes = roleNode.getAttributes();
						for (int k = 0; k < roleAttributes.getLength(); k++) {
							Node roleAttributeNode = roleAttributes.item(k);
							if (roleAttributeNode.getNodeName().equals("rolename")) {
								role.setRolename(roleAttributeNode.getNodeValue());
							} else if (roleAttributeNode.getNodeName().equals("realname")) {
								role.setRealname(roleAttributeNode.getNodeValue());
							}
						}
					}

				}
			}

			// parse information types node
			else if (userDataChildNode.getNodeName().equals("InformationTypes")) {
				Node informationTypesNode = userDataChildNode;

				// parse information types node children
				NodeList informationTypesChildNodes = informationTypesNode.getChildNodes();
				for (int j = 0; j < informationTypesChildNodes.getLength(); j++) {
					Node informationTypesChildNode = informationTypesChildNodes.item(j);

					// parse information types
					if (informationTypesChildNode.getNodeName().equals("InformationType")) {

						// create information type
						Node informationTypeNode = informationTypesChildNode;
						InformationType informationType = new InformationType();
						userData.getInformationTypes().add(informationType);

						// parse information type attributes
						NamedNodeMap informationTypeAttributes = informationTypeNode.getAttributes();
						for (int k = 0; k < informationTypeAttributes.getLength(); k++) {
							Node informationTypeAttributeNode = informationTypeAttributes.item(k);
							if (informationTypeAttributeNode.getNodeName().equals("typename")) {
								informationType.setTypename(informationTypeAttributeNode.getNodeValue());
							} else if (informationTypeAttributeNode.getNodeName().equals("realname")) {
								informationType.setRealname(informationTypeAttributeNode.getNodeValue());
							}
						}

						// parse information type children
						NodeList informationTypeChildNodes = informationTypeNode.getChildNodes();
						for (int k = 0; k < informationTypeChildNodes.getLength(); k++) {
							Node informationTypeChildNode = informationTypeChildNodes.item(k);

							// parse default authorised roles
							if (informationTypeChildNode.getNodeName().equals("AuthorisedRoleDefault")) {

								// create default authorised role
								Node authorisedRoleDefaultNode = informationTypeChildNode;
								String authorisedRoleDefault = authorisedRoleDefaultNode.getTextContent();
								informationType.getAuthorisedRolesDefault().add(authorisedRoleDefault);
							}
						}

					}

				}
			}

			// parse users node
			else if (userDataChildNode.getNodeName().equals("Users")) {
				Node usersNode = userDataChildNode;

				// parse users node children
				NodeList usersChildNodes = usersNode.getChildNodes();
				for (int j = 0; j < usersChildNodes.getLength(); j++) {
					Node usersChildNode = usersChildNodes.item(j);

					// parse guest users
					if (usersChildNode.getNodeName().equals("GuestUser")) {

						// create guest user
						Node guestUserNode = usersChildNode;
						User guestUser = new User();
						userData.setGuestUser(guestUser);

						// parse guest user attributes
						NamedNodeMap guestUserAttributes = guestUserNode.getAttributes();
						for (int k = 0; k < guestUserAttributes.getLength(); k++) {
							Node guestUserAttributeNode = guestUserAttributes.item(k);
							if (guestUserAttributeNode.getNodeName().equals("username")) {
								guestUser.setUsername(guestUserAttributeNode.getNodeValue());
							} else if (guestUserAttributeNode.getNodeName().equals("password")) {
								guestUser.setPassword(guestUserAttributeNode.getNodeValue());
							} else if (guestUserAttributeNode.getNodeName().equals("realname")) {
								guestUser.setRealname(guestUserAttributeNode.getNodeValue());
							} else if (guestUserAttributeNode.getNodeName().equals("rolename")) {
								guestUser.setRolename(guestUserAttributeNode.getNodeValue());
							} else if (guestUserAttributeNode.getNodeName().equals("timeLastLogin")) {
								guestUser.setTimeLastLogin(Long.valueOf(guestUserAttributeNode.getNodeValue()));
							} else if (guestUserAttributeNode.getNodeName().equals("timeLastLogout")) {
								guestUser.setTimeLastLogout(Long.valueOf(guestUserAttributeNode.getNodeValue()));
							}
						}
					}

					// parse users
					else if (usersChildNode.getNodeName().equals("User")) {

						// create user
						Node userNode = usersChildNode;
						User user = new User();
						userData.getUsers().add(user);

						// parse user attributes
						NamedNodeMap userAttributes = userNode.getAttributes();
						for (int k = 0; k < userAttributes.getLength(); k++) {
							Node userAttributeNode = userAttributes.item(k);
							if (userAttributeNode.getNodeName().equals("username")) {
								user.setUsername(userAttributeNode.getNodeValue());
							} else if (userAttributeNode.getNodeName().equals("password")) {
								user.setPassword(userAttributeNode.getNodeValue());
							} else if (userAttributeNode.getNodeName().equals("realname")) {
								user.setRealname(userAttributeNode.getNodeValue());
							} else if (userAttributeNode.getNodeName().equals("rolename")) {
								user.setRolename(userAttributeNode.getNodeValue());
							} else if (userAttributeNode.getNodeName().equals("timeLastLogin")) {
								user.setTimeLastLogin(Long.valueOf(userAttributeNode.getNodeValue()));
							} else if (userAttributeNode.getNodeName().equals("timeLastLogout")) {
								user.setTimeLastLogout(Long.valueOf(userAttributeNode.getNodeValue()));
							}
						}

						// parse user children
						NodeList userChildNodes = userNode.getChildNodes();
						for (int k = 0; k < userChildNodes.getLength(); k++) {
							Node userChildNode = userChildNodes.item(k);

							// parse information items
							if (userChildNode.getNodeName().equals("InformationItem")) {

								// create information item
								Node informationItemNode = userChildNode;
								InformationItem informationItem = new InformationItem();
								user.getInformationItems().add(informationItem);

								// parse information item attributes
								NamedNodeMap informationItemAttributes = informationItemNode.getAttributes();
								for (int l = 0; l < informationItemAttributes.getLength(); l++) {
									Node informationItemAttributeNode = informationItemAttributes.item(l);
									if (informationItemAttributeNode.getNodeName().equals("id")) {
										long id = Long.valueOf(informationItemAttributeNode.getNodeValue());
										informationItem.setId(id);
									} else if (informationItemAttributeNode.getNodeName().equals("typename")) {
										String typename = informationItemAttributeNode.getNodeValue();
										informationItem.setTypename(typename);
									} else if (informationItemAttributeNode.getNodeName().equals("timeProvided")) {
										long timeProvided = Long.valueOf(informationItemAttributeNode.getNodeValue());
										informationItem.setTimeProvided(timeProvided);
									}
								}

								// parse information item children
								NodeList informationItemChildNodes = informationItemNode.getChildNodes();
								for (int l = 0; l < informationItemChildNodes.getLength(); l++) {
									Node informationItemChildNode = informationItemChildNodes.item(l);

									// parse authorised roles
									if (informationItemChildNode.getNodeName().equals("AuthorisedRole")) {

										// create authorised role
										Node authorisedRoleNode = informationItemChildNode;
										String authorisedRole = authorisedRoleNode.getTextContent();
										informationItem.getAuthorisedRoles().add(authorisedRole);
									}

									// parse authorised users
									else if (informationItemChildNode.getNodeName().equals("AuthorisedUser")) {

										// create authorised user
										Node authorisedUserNode = informationItemChildNode;
										String authorisedUser = authorisedUserNode.getTextContent();
										informationItem.getAuthorisedUsers().add(authorisedUser);
									}

									// parse content
									else if (informationItemChildNode.getNodeName().equals("Content")) {

										// create content
										Node contentNode = informationItemChildNode;
										String content = contentNode.getTextContent();
										informationItem.setContent(content);
									}

									// parse requests
									else if (informationItemChildNode.getNodeName().equals("Request")) {

										// create request
										Node requestNode = informationItemChildNode;
										Request request = new Request();
										informationItem.getRequests().add(request);

										// parse request attributes
										NamedNodeMap requestAttributes = requestNode.getAttributes();
										for (int m = 0; m < requestAttributes.getLength(); m++) {
											Node requestAttributeNode = requestAttributes.item(m);
											if (requestAttributeNode.getNodeName().equals("time")) {
												long time = Long.valueOf(requestAttributeNode.getNodeValue());
												request.setTime(time);
											} else if (requestAttributeNode.getNodeName().equals("username")) {
												String username = requestAttributeNode.getNodeValue();
												request.setUsername(username);
											} else if (requestAttributeNode.getNodeName().equals("authorised")) {
												boolean authorised = Boolean.parseBoolean(requestAttributeNode.getNodeValue());
												request.setAuthorised(authorised);
											}
										}
									}

								}
							}
						}

					}
				}
			}

		}

		return userData;
	}

	private Document writeDocument(UserData userData) {

		// document
		Document doc = null;

		try {
			DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbFac.newDocumentBuilder();
			doc = docBuilder.newDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// user data
		Element userDataElement = doc.createElement("UserData");
		userDataElement.setAttribute("nextId", String.valueOf(userData.getNextId()));
		doc.appendChild(userDataElement);

		// roles
		Element rolesElement = doc.createElement("Roles");
		userDataElement.appendChild(rolesElement);
		for (Role role : userData.getRoles()) {
			Element roleElement = doc.createElement("Role");
			roleElement.setAttribute("rolename", role.getRolename());
			roleElement.setAttribute("realname", role.getRealname());
			rolesElement.appendChild(roleElement);
		}

		// information types
		Element informationTypesElement = doc.createElement("InformationTypes");
		userDataElement.appendChild(informationTypesElement);
		for (InformationType informationType : userData.getInformationTypes()) {
			Element informationTypeElement = doc.createElement("InformationType");
			informationTypeElement.setAttribute("typename", informationType.getTypename());
			informationTypeElement.setAttribute("realname", informationType.getRealname());
			informationTypesElement.appendChild(informationTypeElement);

			// default authorised roles
			for (String authorisedRoleDefault : informationType.getAuthorisedRolesDefault()) {
				Element authorisedRoleDefaultElement = doc.createElement("AuthorisedRoleDefault");
				authorisedRoleDefaultElement.setTextContent(authorisedRoleDefault);
				informationTypeElement.appendChild(authorisedRoleDefaultElement);
			}
		}

		Element usersElement = doc.createElement("Users");
		userDataElement.appendChild(usersElement);

		// guest user
		Element guestUserElement = doc.createElement("GuestUser");
		guestUserElement.setAttribute("username", userData.getGuestUser().getUsername());
		guestUserElement.setAttribute("password", userData.getGuestUser().getPassword());
		guestUserElement.setAttribute("realname", userData.getGuestUser().getRealname());
		guestUserElement.setAttribute("rolename", userData.getGuestUser().getRolename());
		guestUserElement.setAttribute("timeLastLogin", String.valueOf(userData.getGuestUser().getTimeLastLogin()));
		guestUserElement.setAttribute("timeLastLogout", String.valueOf(userData.getGuestUser().getTimeLastLogout()));
		usersElement.appendChild(guestUserElement);

		// users
		for (User user : userData.getUsers()) {
			Element userElement = doc.createElement("User");
			userElement.setAttribute("username", user.getUsername());
			userElement.setAttribute("password", user.getPassword());
			userElement.setAttribute("realname", user.getRealname());
			userElement.setAttribute("rolename", user.getRolename());
			userElement.setAttribute("timeLastLogin", String.valueOf(user.getTimeLastLogin()));
			userElement.setAttribute("timeLastLogout", String.valueOf(user.getTimeLastLogout()));
			usersElement.appendChild(userElement);

			// information items
			for (InformationItem informationItem : user.getInformationItems()) {
				Element informationItemElement = doc.createElement("InformationItem");
				informationItemElement.setAttribute("id", String.valueOf(informationItem.getId()));
				informationItemElement.setAttribute("typename", informationItem.getTypename());
				informationItemElement.setAttribute("timeProvided", String.valueOf(informationItem.getTimeProvided()));
				userElement.appendChild(informationItemElement);

				// authorised roles
				for (String authorisedRole : informationItem.getAuthorisedRoles()) {
					Element authorisedRoleElement = doc.createElement("AuthorisedRole");
					authorisedRoleElement.setTextContent(authorisedRole);
					informationItemElement.appendChild(authorisedRoleElement);
				}

				// authorised users
				for (String authorisedUser : informationItem.getAuthorisedUsers()) {
					Element authorisedUserElement = doc.createElement("AuthorisedUser");
					authorisedUserElement.setTextContent(authorisedUser);
					informationItemElement.appendChild(authorisedUserElement);
				}

				// content
				Element contentElement = doc.createElement("Content");
				contentElement.setTextContent(informationItem.getContent());
				informationItemElement.appendChild(contentElement);

				// requests
				for (Request request : informationItem.getRequests()) {
					Element requestElement = doc.createElement("Request");
					requestElement.setAttribute("time", String.valueOf(request.getTime()));
					requestElement.setAttribute("username", request.getUsername());
					requestElement.setAttribute("authorised", String.valueOf(request.isAuthorised()));
					informationItemElement.appendChild(requestElement);
				}
			}
		}

		return doc;
	}

	public void saveXML(UserData userData, String outputFilename) {
		try {
			TransformerFactory transFac = TransformerFactory.newInstance();
			Transformer transformer = transFac.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter stringWriter = new StringWriter();
			StreamResult result = new StreamResult(stringWriter);
			Document doc = writeDocument(userData);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			String xmlString = stringWriter.toString();
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFilename));
			out.write(xmlString);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (WRITE_BAK) {
			copyFile(outputFilename, outputFilename + SUFFIX_BAK);
		}
	}

	private void copyFile(String filenameSrc, String filenameDst) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filenameSrc));
			BufferedWriter out = new BufferedWriter(new FileWriter(filenameDst));
			String line;
			while ((line = in.readLine()) != null) {
				out.write(line);
				out.newLine();
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
