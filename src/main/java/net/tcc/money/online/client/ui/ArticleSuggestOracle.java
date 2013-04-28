package net.tcc.money.online.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.tcc.money.online.shared.dto.Article;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

public class ArticleSuggestOracle extends MultiWordSuggestOracle {

	private final Map<String, Article> suggestionMap = new HashMap<String, Article>();

	@Override
	public void requestSuggestions(Request request, final Callback callback) {
		super.requestSuggestions(request, new Callback() {
			@Override
			public void onSuggestionsReady(Request request, Response response) {
				callback.onSuggestionsReady(request, modifyResponse(response));
			}

		});
	}

	private Response modifyResponse(Response response) {
		ArrayList<ArticleSuggestion> articleSuggestions = new ArrayList<ArticleSuggestion>();
		for (Suggestion s : response.getSuggestions()) {
			MultiWordSuggestion suggestion = (MultiWordSuggestion) s;
			Article article = this.suggestionMap.get(suggestion.getReplacementString());
			if (article == null)
				throw new RuntimeException();
			articleSuggestions.add(new ArticleSuggestion(article, suggestion.getDisplayString()));
		}
		response.setSuggestions(articleSuggestions);
		return response;
	}

	public void add(Article article) {
		String suggestion = generateSuggestionFor(article);
		this.suggestionMap.put(suggestion, article);
		super.add(suggestion);
	}

	private String generateSuggestionFor(Article article) {
		StringBuilder buf = new StringBuilder();
		if (article.getLotSize() != null && !"".equals(article.getLotSize())){
			buf.append("[").append(article.getLotSize()).append("] ");
		}
		if (article.isVegan()){
			buf.append("Bio ");
		}
		buf.append(article.getName());
		if (article.getBrand() != null && !"".equals(article.getBrand())) {
			buf.append(" von ").append(article.getBrand());
		}
		String suggestion = buf.toString();
		return suggestion;
	}

	public static class ArticleSuggestion extends MultiWordSuggestion {

		private final Article article;

		public ArticleSuggestion(Article article, String displayString) {
			super(article.getName(), displayString);
			this.article = article;
		}

		public Article getArticle() {
			return this.article;
		}

	}

}
