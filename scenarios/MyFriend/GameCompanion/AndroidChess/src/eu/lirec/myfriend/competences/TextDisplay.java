package eu.lirec.myfriend.competences;

import eu.lirec.myfriend.events.Successful;
import eu.lirec.myfriend.requests.Say;
import android.os.Handler;
import android.widget.TextSwitcher;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

public class TextDisplay extends Competence {

	private TextSwitcher textSwitcher;
	private Handler handlerUI;
	
	public TextDisplay(TextSwitcher view, Handler handlerUI) {
		this.textSwitcher = view;
		this.getRequestHandlers().add(new PrintText());
		this.handlerUI = handlerUI;
	}
	
	@Override
	public void onDestroy() {
	}

	private class PrintText extends RequestHandler{
		
		public PrintText() {
			super(new TypeSet(Say.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			for (Say request : requests.get(Say.class)) {
				handlerUI.post(new UITextUpdater(request.text));
				//TODO Instead of only setting the first request, make a queue.
				break;
			}
		}
	}
	
	private class UITextUpdater implements Runnable{

		private String text;
		
		public UITextUpdater(String text) {
			this.text = text;
		}
		
		@Override
		public void run() {
			textSwitcher.setText(text);
			handlerUI.removeCallbacks(textCleanerUI);
			handlerUI.postDelayed(textCleanerUI,3000);
		}
	}
	
	private Runnable textCleanerUI =  new Runnable(){
		@Override
		public void run() {
			textSwitcher.setText("");
			raise(new Successful());
		}
	};
}
