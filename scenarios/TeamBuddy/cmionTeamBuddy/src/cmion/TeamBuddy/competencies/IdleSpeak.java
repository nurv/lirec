package cmion.TeamBuddy.competencies;

import java.net.*;
import java.util.HashMap;
import java.util.Random;
import java.io.*;

import cmion.TeamBuddy.competencies.RssParser.RssFeed;
import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;

public class IdleSpeak extends Competency {

	private Random random;

	private static final String BB_IDLE_SPEAK_UTTERANCE = "IdleSpeakUtterance";

	public IdleSpeak(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "IdleSpeak";
		this.competencyType = "IdleSpeak";
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	public void initialize() {
		available = true;
		random = new Random();
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException {

		String idleSpeakUtterance = "";
		double r = random.nextDouble();

		if (r >= 0.0 && r < 0.5) {

			// public information

			String publicInfo = "";
			try {
				URL url = new URL("http://localhost:8080/?action=publicInfo");
				URLConnection urlConnection = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				String input = null;
				while ((input = in.readLine()) != null) {
					publicInfo += input;
				}
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!publicInfo.equals("")) {
				idleSpeakUtterance = publicInfo;
			} else {
				r = 0.5;
			}

		}

		if (r >= 0.5 && r < 0.9) {

			// news

			RssParser rp = new RssParser("http://news.google.co.uk/?output=rss&q=Autonomous+Robots");
			rp.parse();
			RssFeed feed = rp.getFeed();

			if (feed.items.size() > 0) {
				int index = Math.abs(random.nextInt()) % feed.items.size();
				idleSpeakUtterance = "Have you heard about this news? ";
				idleSpeakUtterance += feed.items.get(index).title;
			} else {
				r = 0.9;
			}

		}

		if (r >= 0.9 && r < 1.0) {

			// jokes

			// http://homepage-tools.com/jokes:rss.html
			RssParser rp = new RssParser("http://jokes4all.net/rss/340001511/jokes.xml");
			rp.parse();
			RssFeed feed = rp.getFeed();

			if (feed.items.size() > 0) {
				int index = Math.abs(random.nextInt()) % feed.items.size();
				idleSpeakUtterance = "I know a good joke! ";
				idleSpeakUtterance += feed.items.get(index).description;
			} else {
				idleSpeakUtterance = "I am just talking to keep my affiliation high!";
			}

		}

		// write to BB
		architecture.getBlackBoard().requestSetProperty(BB_IDLE_SPEAK_UTTERANCE, idleSpeakUtterance);

		return true;
	}

}
