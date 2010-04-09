/*
 This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package eu.lirec.myfriend;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import cz.chess.mind.Minimax;
import cz.chess.rules.Position;
import eu.lirec.myfriend.events.BoardChanged;
import eu.lirec.myfriend.events.MovePlayed;
import eu.lirec.myfriend.events.MoveUndone;

public class ChessView extends View {
	int mcx = 4;
	int mcy = 4;
	int mox = -1;
	int moy = -1;
    int mArea = -1;
	boolean mRotated = false;
	boolean mWhitePieces = true;
	boolean mBlackPieces = false;
	boolean mThinking = false;
	Drawable mFigures[][];
	Position mPosition = new Position();
	final Handler mHandler = new Handler();
	byte[] mSchPriThoughts = new byte[Position.h8 + 1];
	
	private ChessUser user;
	private ChessBoard board;
	private Move lastMove;
	
	protected boolean playsPiece() {
		return !isThinking() && (mWhitePieces && mPosition.white ||
			mBlackPieces && !mPosition.white);
	}
	
    public ChessView(Activity a) {
        super(a);
        setFocusable(true);
        mFigures = new Drawable[2][];
        mFigures[0] = new Drawable[6];
        mFigures[1] = new Drawable[6];
        mFigures[0][0] = getContext().getResources().getDrawable(R.drawable.cp);
        mFigures[0][1] = getContext().getResources().getDrawable(R.drawable.cj);
        mFigures[0][2] = getContext().getResources().getDrawable(R.drawable.cs);
        mFigures[0][3] = getContext().getResources().getDrawable(R.drawable.cv);
        mFigures[0][4] = getContext().getResources().getDrawable(R.drawable.cd);
        mFigures[0][5] = getContext().getResources().getDrawable(R.drawable.ck);
        mFigures[1][0] = getContext().getResources().getDrawable(R.drawable.bp);
        mFigures[1][1] = getContext().getResources().getDrawable(R.drawable.bj);
        mFigures[1][2] = getContext().getResources().getDrawable(R.drawable.bs);
        mFigures[1][3] = getContext().getResources().getDrawable(R.drawable.bv);
        mFigures[1][4] = getContext().getResources().getDrawable(R.drawable.bd);
        mFigures[1][5] = getContext().getResources().getDrawable(R.drawable.bk);
        prepareMoveNow();
        user = null;
        board = null;
    }
    
    public ChessView(Activity a, ChessUser user, ChessBoard board){
    	this(a);
    	this.user = user;
    	this.board = board;
    	this.board.getEventHandlers().add(new DrawBoardChanges());
    }
    
    public void prepareMove() {
    	mHandler.post(new Runnable() {
			public void run() {
				prepareMoveNow();
			}
    	});
    }
    
    public void go(int move) {
    	mox = -1;
    	moy = -1;
    	mPosition.move(move, true, true, null);
    	mPosition.findMoves();
    	invalidate();
    	prepareMove();
    }
    
    protected void prepareMoveNow() {
    	if (playsPiece()) {
    		mPosition.findMoves();
    	} else {
			tahniPrograme();
    	}
    	
    }
    
    public void rotate() {
    	mRotated = !mRotated;
    	invalidate();
    }
    
    @Override
	public boolean onTouchEvent(MotionEvent event)  {
    	if (isThinking()) return false;
     	if (event.getAction() != MotionEvent.ACTION_DOWN) return false;
    	if (mArea <= 0 || !playsPiece()) return false;
    	int x = (int)((event.getX() + 0.5) / mArea);
    	int y = (int)((event.getY() + 0.5) / mArea);
    	if (mRotated) {
    		x = 7 - x;
    	} else {
    		y = 7 - y;
    	}
    	
    	if(x < 0 || x > 7 || y < 0 || y > 7){
    		return false;
    	}
    	
    	if(mox == -1 && moy == -1){
    		if(board.getPieceAt(x, y) != null){
    			mox = x;
        		moy = y;
        		lastMove = null;
        		invalidate();
    		}
    		return true;
    	}
    	
    	if(mox == x && moy == y){
    		mox = -1;
    		moy = -1;
    		invalidate();
    		return true;
    	}
    	
    	this.user.makeMove(new Location(mox,moy), new Location(x,y));
    	mox = -1;
		moy = -1;
    	return true;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	
    	Rect r = new Rect();
    	getDrawingRect(r);
  		int w = r.right - r.left;//canvas.getWidth();
   		int h = r.bottom - r.top;//canvas.getHeight() - 50;
        mArea = (w < h ? w : h);
        mArea >>= 3;
        
        Paint white = new Paint();
        white.setARGB(255, 200, 200, 200);
        Paint black = new Paint();
        black.setARGB(255, 100, 100, 100);
        Paint blue = new Paint();
        blue.setARGB(255, 0, 0, 255);
        Paint green = new Paint();
        green.setARGB(255, 0, 255, 0);
        Paint brightWhite = new Paint();
        brightWhite.setARGB(255, 255, 255, 255);
        Paint darkBlack = new Paint();
        darkBlack.setARGB(255, 0, 0, 0);
        boolean clovek = playsPiece();
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
            	Paint p;
            	if (clovek && mox == i && moy == j) {
            		p = green;
            	} else if(lastMove != null && lastMove.getFrom().x == i && lastMove.getFrom().y == j){
            		p = green;
            	} else if(lastMove != null && lastMove.getTo().x == i && lastMove.getTo().y == j){
            		p = blue;
            	} else
            	/*if (clovek && mcx == i && mcy == j) {
            		p = blue;
            	} else*/ {
            		p = ((((i + j) & 1) == 1) ? white : black);
            	}
            	int sx = (mRotated ? 7 - i : i) * mArea;
            	int sy = (mRotated ? j : 7 - j) * mArea;
                canvas.drawRect(new Rect(sx, sy, sx + mArea, sy + mArea), p);
                Piece piece = board.getPieceAt(i, j);
                
                
                if (piece != null){
                	Drawable dr = getPieceDrawable(piece);
                	dr.setBounds(sx, sy, sx + mArea, sy + mArea);
                	dr.draw(canvas);
                }
            }
    }

	
	public void dialog(String co) {
		Dialog d = new Dialog(this.getContext());
		d.setTitle(co);
		d.show();
	}
	
	/*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
    	if (isThinking()) return false;
    	switch (keyCode) {
    	case KeyEvent.KEYCODE_DPAD_UP:
    		if (!mRotated && mcy < 7 || mRotated && mcy > 0) {
    			if (mRotated) mcy--; else mcy++;
    			invalidate();
    		}
    		return true;
    	
    	case KeyEvent.KEYCODE_DPAD_DOWN:
    		if (mRotated && mcy < 7 || !mRotated && mcy > 0) {
    			if (mRotated) mcy++; else mcy--;
    			invalidate();
    		}
    		return true;
    	case KeyEvent.KEYCODE_DPAD_RIGHT:
    		if (!mRotated && mcx < 7 || mRotated && mcx > 0) {
    			if (mRotated) mcx--; else mcx++;
    			invalidate();
    		}
    		return true;
    	case KeyEvent.KEYCODE_DPAD_LEFT:
    		if (mRotated && mcx < 7 || !mRotated && mcx > 0) {
    			if (mRotated) mcx++; else mcx--;
    			invalidate();
    		}
    		return true;
    	case KeyEvent.KEYCODE_DPAD_CENTER:
    		if (!playsPiece()) return true;
    		Vector t = mPosition.findMoves();
    		int pole = Position.a1 + mcx + 10 * mcy;
    		if (mPosition.isThere1(t, pole)) {
    			mox = mcx;
    			moy = mcy;
    			invalidate();
    			return true;
    		}
    		int pole1 = Position.a1 + mox + 10 * moy;
    		if (mPosition.isThere2(t, pole1, pole)) {
    			int tah = mPosition.buildMove(t, pole1, pole);
    			go(tah);
    			return true;
    		}
    		
    		return true;
    		
    	case KeyEvent.KEYCODE_9:
    		System.out.println("killing piece");
    		Piece[][] boardCopy = board.toArray();
    		for (int i = 0; i < boardCopy.length; i++) {
    			for (int j = 0; j < boardCopy[i].length; j++) {
					if(boardCopy[i][j] != null){
						boardCopy[i][j] = null;
						invalidate();
						return true;
					}
				}
				
			}
    		return true;
    	}
    	return false;
    }
    */
    
    public boolean isThinking() {
    	return mThinking;
    }
    
    
    protected void tahniPrograme() {
    	mThinking = true;
    	System.arraycopy(mPosition.sch, 0, mSchPriThoughts, 0, Position.h8 + 1);
    	Thread t = new Thread() {
    		@Override
			public void run() {
    			 mPosition.findMoves();
    			 final int move;
    			 move = Minimax.minimax(mPosition, 5000)[0]; 
    			 mHandler.post(
    					 new Runnable() {

							public void run() {
								mThinking = false;
								if (move != 0) 
									go(move);
								else
									dialog("The end");
							}}
    					 );
    		 }
    	};
    	t.start();
     }
    
    protected void newGame() {
    	mPosition = new Position();
    	invalidate();
    }
    
    protected void playNow() {
    	if (mPosition.white) {
    		mWhitePieces = false;
    		mBlackPieces = true;
    	} else {
    		mWhitePieces = true;
    		mBlackPieces = false;
    	}
    //	this.mcx = this.mcy = this.mcx = this.mcy  
    	invalidate();
    	tahniPrograme();
    }
    
    private Drawable getPieceDrawable(Piece piece) {
		if (piece == null) {
			return null;
		}

		int colour;
		int type;

		switch (piece.getColour()) {
		case Black:
			colour = 0;
			break;

		case White:
			colour = 1;
			break;

		default:
			return null;
		}

		switch (piece.getType()) {
		case Pawn:
			type = 0;
			break;

		case Knight:
			type = 1;
			break;

		case Bishop:
			type = 2;
			break;

		case Rook:
			type = 3;
			break;

		case Queen:
			type = 4;
			break;

		case King:
			type = 5;
			break;

		default:
			return null;
		}

		return mFigures[colour][type];
	}
    
    private class DrawBoardChanges extends EventHandler {

		protected DrawBoardChanges() {
			super(BoardChanged.class);
		}

		@Override
		public void invoke(IEvent evt) {
			
			try{
				evt.getClass().asSubclass(MovePlayed.class);
				MovePlayed move = (MovePlayed) evt;
				lastMove = new Move(move.from, move.to);
			}catch(ClassCastException e){
			}
			
			try{
				evt.getClass().asSubclass(MoveUndone.class);
				lastMove = null;
			}catch(ClassCastException e){
			}
			
			postInvalidate();
		}
    	
    }
}
