import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *りばーし
 */
public class ReversiBit{
	/**
	 * ゲーム数
	 */
	static int GameCount=0;
	/**
	 * マスサイズ
	 */
	int imgsize = 64+32;
	/**
	 * ボードサイズ
	 */
	static final Point Max = new Point(8,8);
	/**
	 * 黒定数
	 */
	static final int Black = 0;
	/**
	 * 白定数
	 */
	static final int White = 1;
	/**
	 * 両チーム定数
	 */
	static final int BW = 2;
	/**
	 * チームなし定数
	 */
	static final int Noteam = 3;

	/**
	 * リバーシオブジェクト作成
	 * @param args null
	 * @throws IOException null
	 */
	public static void main(String[] args) throws IOException {
		ReversiBit RB = new ReversiBit();
	}
	/**
	 * AI思考中
	 */
	boolean AIing=false;
	/**
	 * 一時的に更新
	 */
	boolean Clicking =true;
	/**
	 * AIのチーム
	 * Black White BW Noteam
	 */
	int AIteam = White  ;
	/**
	 * 試合時間計測用
	 */
	long start;
	/**
	 * AIの勝敗数
	 * 0,勝ち　1,負け 2,引き分け
	 */
	static int AIwin[] = {0,0,0};
	/**
	 * メイン戦局情報
	 */
	final Teaminfo ti= new Teaminfo();
	/**
	 * AI用評価パラメーター
	 */
	Parameter Para = new Parameter();
	/**
	 * リバーシ表示ウィンドウ
	 */
	final ReversiGraphic RG = new ReversiGraphic();
	/**
	 * AI用評価パラメーター
	 */
	 class Parameter{
		/**
		 * AI用場所評価パラメータ
		 */
	 	int[] Binfo = new int[64];
		/**
		 * 単純場所ポイント
		 */
		 float Bx= 1.0f;
		/**
		 * 絶対動かせない場合ポイント
		 */
		 float Ne = 1.5f;
		/**
		 * 置き場所ポイント倍率
		 */
		 float Myp =5.0f;
		/**
		 * 自分が置ける場所ポイント倍率
		 */
		 float CanMe = 1.5f;
		/**
		 * 敵が置ける場所ポイント倍率
		 */
		 float CanEn = 3f;
		/**
		 * 探索深度
		 */
		final int SetDeep =1+2* 3  ;
		/**
		 * パラメータを最大50％上下します
		 */
		void randomizer() {
			paraAnouncer();
			float num=0.45f;
			System.out.println();
			System.out.println("Randomize!");
			System.out.println();
			Bx	+=	Bx	*(Math.random()-num);
			Ne	+=	Ne	*(Math.random()-num);
			Myp	+=	Myp	*(Math.random()-num);
			CanMe	+=	CanMe	*(Math.random()-num);
			CanEn	+=	CanEn	*(Math.random()-num);
			paraAnouncer();
		}
		/**
		 * パラメータをコンソールに表示します
		 */
		void paraAnouncer() {
			System.out.println("Bx: "		+Bx);
			System.out.println("Ne: "			+Ne);
			System.out.println("Myp: "		+Myp);
			System.out.println("CanMe: "		+CanMe);
			System.out.println("CanEn: "		+CanEn);
			System.out.println("SetDeep: "	+SetDeep);
		}
	}
	/**
	 * 盤面情報　メタ
	 */
	class Teaminfo{
		/**
		 * 何ターン目か、コマ数
		 */
		int gameturn;
		/**
		 * 現ターンに置けるチーム
		 */
		int Nowteam;
		/**
		 * 盤面上の各チームごとのコマ数
		 */
		int Bcount[] = {0,0};
		/**
		 * 何箇所置ける場所があるか
		 * AllCheckerで値が入る
		 */
		int canCount[] = {0,0};
		/**
		 * コマ有無
		 */
		long bitboard[]= {0,0};
		/**
		 * 着手可能場所
		 */
		long bitcanboard[]= {0,0};
	}
	/**
	 * JFrameによるリバーシのグラフィック表示
	 */
	final class ReversiGraphic extends JFrame{
		/**
		 * パネル、マス
		 */
		JPanel[][] jpc = new JPanel[Max.x][Max.y];
		/**
		 * カードレイアウト
		 */
		CardLayout[][] CL = new CardLayout[Max.x][Max.y];
		/**
		 * バックグラウンド用パネル、全体
		 */
		JPanel jpG = new JPanel();
		/**
		 * グリッドバッグレイアウト、コマ配置用
		 */
		 GridBagLayout GBL = new GridBagLayout();
		/**
		 * グリッドバッグ制御用
		 */
		GridBagConstraints gbc = new GridBagConstraints();
		/**
		 * イメージアイコン
		 * 固定８
		 */
		final ImageIcon[] img = new ImageIcon[8];//固定
		/**
		 * カーソル、コマカーソルも
		 */
		final Cursor[] cursor=new Cursor[3];
		/**
		 * リサイズするかどうか
		 */
		boolean Resize=false;
		/**
		 * グラフィック更新タイマー
		 */
		Timer update = new Timer();
		/**
		 * グラフィックコンストラクター
		 */
		ReversiGraphic() {
			setBounds(10,20,imgsize*(Max.x)+16+8,imgsize*(Max.y)+40+8);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			enableEvents( java.awt.AWTEvent.KEY_EVENT_MASK );
			setVisible(true);
			try {
				graphicprepare();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				img[6] =new ImageIcon(ImageIO.read(new File(".\\textures\\black.png"))	.getScaledInstance(64, 64,Image.SCALE_DEFAULT));
				img[7] =new ImageIcon(ImageIO.read(new File(".\\textures\\white.png"))	.getScaledInstance(64, 64,Image.SCALE_DEFAULT));
			} catch (IOException e) {
				e.printStackTrace();
			}

			Point hotSpot = new Point(12,12);
			String name= "Cursor";
			Toolkit kit = Toolkit.getDefaultToolkit();
			Image imgic1 = img[6].getImage();
			cursor[Black] = kit.createCustomCursor(imgic1, hotSpot, name);
			Image imgic2 = img[7].getImage();
			cursor[White] = kit.createCustomCursor(imgic2, hotSpot, name);

			update.schedule(new update(), 30,30);
			Clicking=true;
		}
		/**
		 * 画像準備、解像度変更も
		 * @throws IOException 画像がなかった等
		 */
		void graphicprepare() throws IOException {
			remove(jpG);
			//初期化
			jpc = new JPanel[Max.x][Max.y];
			jpG = new JPanel();

			img[0] =new ImageIcon(ImageIO.read(new File(".\\textures\\tile1.png"))	.getScaledInstance(imgsize, imgsize,Image.SCALE_DEFAULT));
			img[1] =new ImageIcon(ImageIO.read(new File(".\\textures\\blackt.png"))	.getScaledInstance(imgsize, imgsize,Image.SCALE_DEFAULT));
			img[2] =new ImageIcon(ImageIO.read(new File(".\\textures\\whitet.png"))	.getScaledInstance(imgsize, imgsize,Image.SCALE_DEFAULT));
			img[3] =new ImageIcon(ImageIO.read(new File(".\\textures\\tileblack.png"))	.getScaledInstance(imgsize, imgsize,Image.SCALE_DEFAULT));
			img[4] =new ImageIcon(ImageIO.read(new File(".\\textures\\tilewhite.png"))	.getScaledInstance(imgsize, imgsize,Image.SCALE_DEFAULT));
			img[5] =new ImageIcon(ImageIO.read(new File(".\\textures\\tilebc.png"))	.getScaledInstance(imgsize, imgsize,Image.SCALE_DEFAULT));

			jpG.setLayout(GBL);

			for(int Y = 0 ; Y < Max.y ; Y++){
				gbc.gridy=Y;
				for(int X = 0 ; X < Max.x ; X++){
					gbc.gridx=X;
					jpc[X][Y]=new JPanel();
					CL[X][Y]=new CardLayout();
					jpc[X][Y].setLayout(CL[X][Y]);
					for(int T=0;T<=5;T++){
						jpc[X][Y].add(new JLabel(img[T]),String.valueOf(T));
					}
					CL[X][Y].first(jpc[X][Y]);
					GBL.setConstraints(jpc[X][Y], gbc);
					jpc[X][Y].addMouseListener(new mouseListener());
					jpG.add(jpc[X][Y]);
					add(jpG);
				}
			}
			Clicking=true;
		}
		/**
		 * ウィンドウタイトル表示
		 */
		public void titleset() {
			float rate=(float)(AIwin[0])/(float)(AIwin[0]+AIwin[1])*100F;
			String Rate=String.format("%.1f", rate);
				setTitle("りばーし　"+GameCount+"プレイ　 AI勝率： "+Rate+"%  盤面："+ti.Bcount[Black]+" - "+ti.Bcount[White]+"  AIチーム:"+teamname(AIteam)+"   現在"+teamname(ti.Nowteam)+"のターン");
		}
		/**
		 * マウスアイコンを現在チームのものに
		 */
		void mouseicon(){
			setCursor(cursor[ti.Nowteam]);
		}
		/**
		 * マス目を実際に表示
		 * @param X　X
		 * @param Y　Y
		 * @param v  イメージID
		 */
		void doshow(int X,int Y,int v) {
			CL[X][Y].show(jpc[X][Y], String.valueOf(v));
		}
		/**
		 * キーボード
		 * @param k key
		 */
		@Override
		protected void processKeyEvent(java.awt.event.KeyEvent k){
			if(k.getID() == java.awt.event.KeyEvent.KEY_PRESSED){
	//			System.out.println(" key"+k.getKeyCode());
				switch( k.getKeyCode()) {
					case java.awt.event.KeyEvent.VK_SPACE:
						if(AIing==false)
							AIrun();
						break;
					case java.awt.event.KeyEvent.VK_S:
						saveBoard();break;
					case java.awt.event.KeyEvent.VK_D:
						loadBoard();break;
					case java.awt.event.KeyEvent.VK_A:
						RG.repaint();
						AllChecker(ti);
						Clicking=true;
						break;
//					case java.awt.event.KeyEvent.VK_B:
//						ReversiStart();
//						break;
					case java.awt.event.KeyEvent.VK_G:
						Para.randomizer();
						break;
//					case java.awt.event.KeyEvent.VK_Q:
//						new ReversiBit();
//						break;
					case java.awt.event.KeyEvent.VK_Z:
						RG.dispose();
						new ReversiBit();
						break;
				}
			}
		}
		/**
		 * 画面更新タイマー
		 */
		class update extends TimerTask{
			update(){
				repaint();
			}
			/**
			 * scheduleよりタイマー実行
			 */
			@Override
			public void run() {
				if(Resize) {
					try {
						graphicprepare();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Resize=false;
					Clicking=true;
					repaint();
				}
				if(Clicking) {
					Clicking=false;
					mouseicon();
					titleset();
					int X=0,Y=0;
					for(long mask=1; mask!=0L ; mask<<=1){
						if((ti.bitboard[Black] & mask) != 0){
							doshow(X,Y, 1);
						}else if((ti.bitboard[White] & mask) != 0){
							doshow(X,Y, 2);
						}else if((ti.bitcanboard[Black] & ti.bitcanboard[White] & mask) != 0){
							doshow(X,Y, 5);
						}else if((ti.bitcanboard[Black] & mask) != 0){
							doshow(X,Y, 3);
						}else if((ti.bitcanboard[White] & mask) != 0){
							doshow(X,Y, 4);
						}else{
							doshow(X,Y, 0);
						}
						if(++X>=8){
							X=0;
							Y++;
						}
					}
				}
			}
		}
	}
	/**
	 * リバーシコンストラクター
	 * 初期設定も
	 */
	ReversiBit(){
		System.out.println("りばーし");
		try {
			loadLearning();
			loadCount();
		} catch (IOException e) {

		}
		String selectvalues[] = {"黒", "白", "両方", "なし"};

		int select = JOptionPane.showOptionDialog(RG,
				"どれをプレイ？",
				"Setting",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				selectvalues,
				selectvalues[0]
				);
		switch(select) {
		case 0:
			AIteam=White;
			break;
		case 1:
			AIteam=Black;
			break;
		case 2:
			AIteam=Noteam;
			break;
		case 3:
			AIteam=BW;
			break;
		}
		ReversiStart();
		RG.addComponentListener( new windowcomp() );
	}
	/**
	 * スタート
	 * 初期化
	 */
	public void ReversiStart(){
		ti.Nowteam = 0;
		ti.gameturn = 0;

		ti.bitboard[Black] = 0x0000000810000000L;
		ti.bitboard[White] = 0x0000001008000000L;

		start = System.currentTimeMillis();

		AllChecker(ti);
		Clicking=true;
		if(AIteam==BW || AIteam==Black)AIrun();
	}
	/**
		 * ゲームセット
		 */
	void Gameset(){
		AIing=false;

		int winner=getWinner(ti);
		if(winner==BW){
			System.out.println("Draw Game");
			if(AIteam==Black||AIteam==White)
				AIwin[BW]++;
		}else if(AIteam==Noteam) {
		}else if(winner==AIteam) {
			System.out.println("AI WIN");
			AIwin[0]++;
		}else if(winner==enemy(AIteam)) {
			System.out.println("AI LOSE");
			AIwin[1]++;
		}

		System.out.println(teamname(winner)+"の勝ち！"+"    "+teamname(Black)+ti.Bcount[Black]+" : "+ti.Bcount[White]+teamname(White));
		GameCount++;
		saveCount();
		long end = System.currentTimeMillis();
		System.out.println("Time:" + (end - start)  + "ms");
	}
	/**
	 * 着手可能ボードを返す
	 * @param myteam　どのチームの着手可能場所か
	 * @param enemy　敵ボード
	 * @return　着手可能ボード
	 */
	long getCanBoard(long myteam, long enemy){
		//左右端の番人
		long horizontalWatchBoard = enemy & 0x7e7e7e7e7e7e7e7eL;
		//上下端の番人
		long verticalWatchBoard   = enemy & 0x00FFFFFFFFFFFF00L;
		//全辺の番人
		long allSideWatchBoard    = enemy & 0x007e7e7e7e7e7e00L;
		//空きマスのみにビットが立っているボード
		long blankBoard = ~(myteam | enemy);
		//隣に相手の色があるかを一時保存する
		long tmp;
		//返り値
		long legalBoard;

		//8方向チェック (・一度に返せる石は最大6つ ・高速化のためにforを展開)
		//左
		tmp = horizontalWatchBoard & (myteam << 1);
		tmp |= horizontalWatchBoard & (tmp << 1);
		tmp |= horizontalWatchBoard & (tmp << 1);
		tmp |= horizontalWatchBoard & (tmp << 1);
		tmp |= horizontalWatchBoard & (tmp << 1);
		tmp |= horizontalWatchBoard & (tmp << 1);
		legalBoard = blankBoard & (tmp << 1);

		//右
		tmp = horizontalWatchBoard & (myteam >>> 1);
		tmp |= horizontalWatchBoard & (tmp >>> 1);
		tmp |= horizontalWatchBoard & (tmp >>> 1);
		tmp |= horizontalWatchBoard & (tmp >>> 1);
		tmp |= horizontalWatchBoard & (tmp >>> 1);
		tmp |= horizontalWatchBoard & (tmp >>> 1);
		legalBoard |= blankBoard & (tmp >>> 1);

		//上
		tmp = verticalWatchBoard & (myteam << 8);
		tmp |= verticalWatchBoard & (tmp << 8);
		tmp |= verticalWatchBoard & (tmp << 8);
		tmp |= verticalWatchBoard & (tmp << 8);
		tmp |= verticalWatchBoard & (tmp << 8);
		tmp |= verticalWatchBoard & (tmp << 8);
		legalBoard |= blankBoard & (tmp << 8);

		//下
		tmp = verticalWatchBoard & (myteam >>> 8);
		tmp |= verticalWatchBoard & (tmp >>> 8);
		tmp |= verticalWatchBoard & (tmp >>> 8);
		tmp |= verticalWatchBoard & (tmp >>> 8);
		tmp |= verticalWatchBoard & (tmp >>> 8);
		tmp |= verticalWatchBoard & (tmp >>> 8);
		legalBoard |= blankBoard & (tmp >>> 8);

		//右斜め上
		tmp = allSideWatchBoard & (myteam << 7);
		tmp |= allSideWatchBoard & (tmp << 7);
		tmp |= allSideWatchBoard & (tmp << 7);
		tmp |= allSideWatchBoard & (tmp << 7);
		tmp |= allSideWatchBoard & (tmp << 7);
		tmp |= allSideWatchBoard & (tmp << 7);
		legalBoard |= blankBoard & (tmp << 7);

		//左斜め上
		tmp = allSideWatchBoard & (myteam << 9);
		tmp |= allSideWatchBoard & (tmp << 9);
		tmp |= allSideWatchBoard & (tmp << 9);
		tmp |= allSideWatchBoard & (tmp << 9);
		tmp |= allSideWatchBoard & (tmp << 9);
		tmp |= allSideWatchBoard & (tmp << 9);
		legalBoard |= blankBoard & (tmp << 9);

		//右斜め下
		tmp = allSideWatchBoard & (myteam >>> 9);
		tmp |= allSideWatchBoard & (tmp >>> 9);
		tmp |= allSideWatchBoard & (tmp >>> 9);
		tmp |= allSideWatchBoard & (tmp >>> 9);
		tmp |= allSideWatchBoard & (tmp >>> 9);
		tmp |= allSideWatchBoard & (tmp >>> 9);
		legalBoard |= blankBoard & (tmp >>> 9);

		//左斜め下
		tmp = allSideWatchBoard & (myteam >>> 7);
		tmp |= allSideWatchBoard & (tmp >>> 7);
		tmp |= allSideWatchBoard & (tmp >>> 7);
		tmp |= allSideWatchBoard & (tmp >>> 7);
		tmp |= allSideWatchBoard & (tmp >>> 7);
		tmp |= allSideWatchBoard & (tmp >>> 7);
		legalBoard |= blankBoard & (tmp >>> 7);

		return legalBoard;
	}
	/**
	 * 点移動
	 * @param mask 指定場所
	 * @param direction 0から7 上から右回り
	 * @return　移動したした指定場所
	 */
	private long transfer(long mask, int direction) {
	    switch(direction){
		    case 0: //上
		        return (mask << 8) & 0xffffffffffffff00L;
		    case 1: //右上
		        return (mask << 7) & 0x7f7f7f7f7f7f7f00L;
		    case 2: //右
		        return (mask >>> 1) & 0x7f7f7f7f7f7f7f7fL;
		    case 3: //右下
		        return (mask >>> 9) & 0x007f7f7f7f7f7f7fL;
		    case 4: //下
		        return (mask >>> 8) & 0x00ffffffffffffffL;
		    case 5: //左下
		        return (mask >>> 7) & 0x00fefefefefefefeL;
		    case 6: //左
		        return (mask << 1) & 0xfefefefefefefefeL;
		    case 7: //左上
		        return (mask << 9) & 0xfefefefefefefe00L;
		    default:
		    	System.err.println("transferErr");
		        return 0;
	    }
	}
	/**
	 * 盤面を見て、置ける場所を探索
	 * チーム交代後に呼び出される
	 * @param tia Teaminfo
	 */
	public void AllChecker(Teaminfo tia){
		tia.bitcanboard[Black] ^= tia.bitcanboard[Black];//=0
		tia.bitcanboard[White] ^= tia.bitcanboard[White];

		tia.bitcanboard[Black]|=getCanBoard(tia.bitboard[Black], tia.bitboard[White]);
		tia.bitcanboard[White]|=getCanBoard(tia.bitboard[White], tia.bitboard[Black]);

		tia.canCount[Black]=Long.bitCount(tia.bitcanboard[Black]);
		tia.canCount[White]=Long.bitCount(tia.bitcanboard[White]);

		tia.Bcount[Black]=Long.bitCount(tia.bitboard[Black]);
		tia.Bcount[White]=Long.bitCount(tia.bitboard[White]);

		if(isEndGame(tia)){//どちらも置けない
			if(AIing==false)
				Gameset();
			return;
		}

		if(tia.canCount[Black]<1&&tia.Nowteam==Black){//パスになる場合
			if(AIing==false)
				System.out.println(teamname(Black)+" PASS");
			teamchanger(tia);
		}else if(tia.canCount[White]<1&&tia.Nowteam==White){
			if(AIing==false)
				System.out.println(teamname(White)+" PASS");
			teamchanger(tia);
		}
	}
	/**
	 * コマ置き　その後チーム交代
	 * @param tia teaminfo
	 * @param mask 置く場所mask
	 * @return 置いた場合true 置けない場合、置かない場合false
	 */
	public boolean Changer(Teaminfo tia,final long mask){
		if( (tia.bitcanboard[tia.Nowteam] & mask)!=0 ) {
			long rev = 0;
			for(int k=0;k<8;k++){
				long revt =  0;
				long mask2 = transfer(mask, k);
				while( (mask2 != 0) && ((mask2 & tia.bitboard[enemy(tia.Nowteam)]) != 0) ){
					revt |= mask2;
					mask2 = transfer(mask2, k);

//					System.out.println("Changing");
				}
				if( (mask2 & tia.bitboard[tia.Nowteam]) != 0 ){
					rev |= revt;
				}
			}
			tia.bitboard[tia.Nowteam]   ^= mask | rev;
			tia.bitboard[enemy(tia.Nowteam)] ^= rev;

			if(!AIing)
				System.out.println(teamname(tia.Nowteam)+" Placed "+getPointMask(mask) );
			teamchanger(tia);
//			System.out.println("Changed");
			return true;
		}else {
//			System.out.println("cannot");
			return false ;
		}
	}
	/**
	 * どちらも打てない状況かチェック
	 * @param tia teaminfo
	 * @return どちらも打てないかどうか
	 */
	public boolean isEndGame(Teaminfo tia){
		if(tia.canCount[Black]>0){
			return false;
		}else if(tia.canCount[White]>0){
			return false;
		}else{
//			System.out.println("isEndgame");
			return true;
		}
	}
	/**
	 * EndGameだとしたときの勝者
	 * @param tia teaminfo
	 * @return 引き分けの場合 BW
	 */
	public int getWinner(Teaminfo tia) {
		if(tia.Bcount[Black]>tia.Bcount[White]) {
			return Black;
		}else if(tia.Bcount[White]>tia.Bcount[Black]) {
			return White;
		}else {
			return BW;
		}
	}
	/**
	 * 相手チームに交代、AllCheckerする
	 * @param tia teaminfo
	 */
	public void teamchanger(Teaminfo tia){
		tia.gameturn++;
		tia.Nowteam=enemy(tia.Nowteam);
		AllChecker(tia);
		if(!AIing)
			Clicking=true;
	}
	/**
	 * maskを返す
	 * @param X X
	 * @param Y Y
	 * @return mask
	 */
	public long getMask(int X,int Y) {
//		System.out.println(X+" "+Y);
		return 1l<<(Y*8)+X;
	}
	/**
	 * IntMaskを返す
	 * @param mask mask
	 * @return １から６３と０
	 */
	public int getIntMask(long mask) {
		int i=Long.numberOfTrailingZeros(mask)+1;
		if(i>=64)i=0;
		return i;
	}
	/**
	 * 表示用の座標を返す
	 * @param mask　mask
	 * @return 場所を表す文字列
	 */
	public String getPointMask(long mask) {
		int IntMask=getIntMask(mask)-1;
		if(IntMask==-1)IntMask=63;
		int X=IntMask%8;
		int Y=IntMask/8;
		String ret=X+" - "+Y;
		return ret;
	}
	/**
	 * 敵チーム
	 * @param team チーム
	 * @return　渡されたチームの敵チーム
	 */
	public int enemy(int team){
		if(team==Black){
			team=White;
		}else if(team==White){
			team=Black;
		}else if(team==BW){
			team=BW;
		}else {
			team=Noteam;
		}
		return(team);
	}
	/**
	 * チーム名
	 * @param team　チーム
	 * @return　チーム名
	 */
	public String teamname(int team){
		if(team==Black){
			return(" 黒 ");
		}else if(team==White){
			return(" 白 ");
		}else if(team==BW){
			return("B & W");
		}else {
			return("None");
		}
	}
	/**
	 * AIをどう動かすか制御
	 */
	public void AIrun(){
		if(isEndGame(ti)){//どちらも置けない
			ReversiStart();
			return;
		}
		boolean withAI;//AIを使うかどうか、使わない場合ランダム
		if(AIteam==BW) {
			withAI=true;
		}else if(ti.Nowteam==AIteam){
			withAI=true;
		}else{
			withAI=false;
		}

		if(withAI==true){			//AI
			AIing=true;
			long DO = ThreadPredictor();
			AIing=false;
			Changer(ti,DO);
		}else{						//ランダム
			int X=0,Y=0;
			do{
			X =(int)(Math.random()*(Max.x));
			Y =(int)(Math.random()*(Max.y));
			}while(Changer(ti,getMask(X,Y))==false);
			System.out.println(teamname(enemy(ti.Nowteam))+"random- "+X+" , "+Y);
		}
		if(isEndGame(ti)){return;}


		//続けてAIrunするとき
		if(ti.Nowteam==AIteam) {
			AIing=true;
			Timer TimerAIb = new Timer();
			TimerAIb.schedule(new TimerAI(), 1000);
		}else if(AIteam==BW) {
			AIing=true;
			Timer TimerAIc = new Timer();
			TimerAIc.schedule(new TimerAI(), 1);
		}
	}
	/**
	 * AI用場面ID、IntMaskごとに
	 */
	int[] parentIds=new int[64];
	/**
	 * IntMaskで
	 */
	ArrayList<Think>[] Thinks = new ArrayList[64];
	/**
	 * スレッディングして、おくべき場所を考える
	 * AIing中
	 * @return 置くべき場所
	 */
	long ThreadPredictor(){
		if(AIing==false) {System.err.println("AIingmiss");}
		ArrayList<Long> candidate = new ArrayList<Long>();
		ArrayList<Integer> cval = new ArrayList<Integer>();
		ArrayList<Think> thi =new ArrayList<Think>();
		for(long mask=1; mask!=0L ; mask<<=1){
			int intmask=getIntMask(mask);
			Thinks[intmask]=new ArrayList<Think>();
			parentIds[intmask]=0;
			if( (ti.bitcanboard[ti.Nowteam] & mask )!=0){
//				System.out.println("Prepare "+X+" , "+Y);
				Think firstT=new Think(mask,Para.SetDeep,parentIds[intmask]);
				thi.add(firstT);
				candidate.add( mask );
			}
		}
		final int MaxThread = Runtime.getRuntime().availableProcessors();
		int Threadnum=0;

		for(int c=0,d=0;c<thi.size();){
			if(Threadnum<MaxThread){
				thi.get(c).start();
				Threadnum++;
				c++;
			}else{
				try {
					thi.get(d).join();
					d++;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Threadnum--;
			}
		}

		while(thi.size()>0){
			try {
				thi.get(0).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			cval.add(thi.get(0).getreplyvalue());
			thi.remove(0);
		}
		int c=MaxMatch(cval);
		long valueP;
		try {
			valueP = candidate.get(c);
		} catch (Exception e) {//どこにも置けなかったら
			System.err.println(ti.gameturn+teamname(ti.Nowteam));
			teamchanger(ti);
			valueP = 0;
			return(valueP);
		}
		System.out.println("(Max)　Point: "+getPointMask(valueP)+"　value: "+cval.get(c));
		return(valueP);
	}
	/**
	 * ArrayListの最大
	 * @param cval int 型ArrayList
	 * @return 最大の値を取るArralListのインデックス
	 */
	public int MaxMatch(ArrayList<Integer> cval){
		int max=Integer.MIN_VALUE;
		int c=0;
		for(int num:cval){
			max = Math.max(max, num);
		}
		while(max!=cval.get(c)) {
			c++;
		}
		return(c);
	}
	/**
	 * ArrayListの最小
	 * @param cval int 型ArrayList
	 * @return 最小の値を取るArralListのインデックス
	 */
	public int minMatch(ArrayList<Integer> cval){
		int min=Integer.MAX_VALUE;
		int c=0;
		for(int num:cval){
			min = Math.min(min, num);
		}
		while(min!=cval.get(c)){
			c++;
		}
		return(c);
	}
	/**
	 * リバーシ思考AI
	 */
	public class Think extends Thread /*implements Cloneable*/{
		/**
		 * 初期置き場 long
		 */
		long FPL;
		/**
		 * 初期置き場 long
		 */
		int FPI;
		/**
		 * 現在深度
		 */
		int deep;
		/**
		 * 初期位置評価
		 */
		int replyvalue;
		/**
		 * 親局面id
		 */
		int myparent;
		/**
		 * 自局面id
		 */
		int myid;
		/**
		 * 自局面の評価
		 */
		int myvalue;
		/**
		 * 探索末端かどうか
		 */
		boolean isEnd =false;
		/**
		 * 戦局、思考用
		 */
		Teaminfo Tti  = new Teaminfo();
		/**
		 * コンストラクター
		 * @param Fmask 着手場所
		 * @param dp 深度
		 * @param parent 親
		 */
		Think(long Fmask,int dp,int parent){
			FPL=Fmask;
			FPI=getIntMask(Fmask);
			deep=dp;
			myparent=parent;

			if(parent==0) {
				Tti.bitboard[Black]=ti.bitboard[Black];
				Tti.bitboard[White]=ti.bitboard[White];
				Tti.bitcanboard[Black]=ti.bitcanboard[Black];
				Tti.bitcanboard[White]=ti.bitcanboard[White];

				Tti.canCount[Black]=ti.canCount[Black];
				Tti.canCount[White]=ti.canCount[White];
				Tti.Bcount[Black]=ti.Bcount[Black];
				Tti.Bcount[White]=ti.Bcount[White];

				Tti.Nowteam=ti.Nowteam;
				Tti.gameturn=ti.gameturn;
			}
		}

		/**
		 * コピー
		 * @return コピーThink、deepは下がる、親はコピー元
		 */
		@Override
		public Think clone() {
				Think res = new Think(FPL, (deep-1), myid);

				res.Tti.bitboard[Black]=Tti.bitboard[Black];
				res.Tti.bitboard[White]=Tti.bitboard[White];
				res.Tti.bitcanboard[Black]=Tti.bitcanboard[Black];
				res.Tti.bitcanboard[White]=Tti.bitcanboard[White];

				res.Tti.canCount[Black]=Tti.canCount[Black];
				res.Tti.canCount[White]=Tti.canCount[White];
				res.Tti.Bcount[Black]=Tti.Bcount[Black];
				res.Tti.Bcount[White]=Tti.Bcount[White];

				res.Tti.Nowteam=Tti.Nowteam;
				res.Tti.gameturn=Tti.gameturn;
				return res;
		}
		/**
		 * 実行、探索最初のオブジェクトのみ
		 */
		@Override
		public void run() {
//			System.out.println("ThreadStart "+FP.x+" , "+FP.y);
			Trydo(FPL,0);
			nextgenerations();//敵の返し
			ArrayList<Integer> vals = new ArrayList<Integer>();
			ArrayList<Think> childs = new ArrayList<Think>();
			int targetdeep=Para.SetDeep;
			for(int c=0;c<Thinks[FPI].size();){
				if(Thinks[FPI].get(c).deep<targetdeep){
					targetdeep--;
				}
				if(Thinks[FPI].get(c).deep==targetdeep && Thinks[FPI].get(c).Tti.Nowteam==enemy(ti.Nowteam)) {
						for(int targetparent = Thinks[FPI].get(c).myparent ; c<Thinks[FPI].size() && targetparent==Thinks[FPI].get(c).myparent ;) {
							childs.add(Thinks[FPI].get(c));
							vals.add(Thinks[FPI].get(c).myvalue);
							if(Thinks[FPI].get(c).isEnd==false) {
								Thinks[FPI].remove(c);
							} else {
								c++;
							}
						}
						int i = MaxMatch(vals);
						childs.get(i).nextgenerations();
						vals.clear();
						childs.clear();
				} else if (Thinks[FPI].get(c).deep==targetdeep && Thinks[FPI].get(c).Tti.Nowteam==ti.Nowteam){
					Thinks[FPI].get(c).nextgenerations();
					if(Thinks[FPI].get(c).isEnd==false)
						Thinks[FPI].remove(c);
					else
						c++;
				}
			}

			//最終評価をする
			int range=Thinks[FPI].size()-1;
			int mindeep=0;
			try {
				mindeep = Thinks[FPI].get(range).deep;
			} catch (IndexOutOfBoundsException e) {
				mindeep=Para.SetDeep-1;
			}
			ArrayList<Integer> mms = new ArrayList<Integer>();
			for(int d=range; d>=0 && Thinks[FPI].get(d).deep==mindeep;d--) {
				mms.add(Thinks[FPI].get(d).myvalue);
			}
			int i = 0;
			try {
				i=minMatch(mms);
				replyvalue=mms.get(i);
			} catch (IndexOutOfBoundsException e) {
//				System.err.println(getPointMask(FPL)+"  mmsError");
				replyvalue=myvalue;
			}
			String BWscore;
			try {
				String equality;
				switch(getWinner(Thinks[FPI].get(i).Tti)) {
					case Black:
						equality=" > ";
						break;
					case White:
						equality=" < ";
						break;
					case BW:
						equality=" = ";
						break;
					default:
						equality=" : ";
						break;
				}
				BWscore=teamname(Black)+String.format("%02d",Thinks[FPI].get(i).Tti.Bcount[Black])+equality+String.format("%02d",Thinks[FPI].get(i).Tti.Bcount[White])+teamname(White);
			} catch (IndexOutOfBoundsException e) {
				BWscore="Finished";
			}
			System.out.println(getPointMask(FPL)+"	value: "+String.format("%5d", replyvalue)+"	"+BWscore);
//			System.out.println("ThreadEnd"+FP.x+","+FP.y);
		}
		/**
		 * 置いてみる、myidと自分の評価を入れる
		 * @param trymask 置く場所mask
		 * @param parent parent(0)
		 */
		void Trydo(final long trymask,final int parent){
			int teamtemp=Tti.Nowteam;
			if(Changer(Tti,trymask)==false) {
				System.err.println("miss at "+trymask);
				return;
			}

			parentIds[FPI]++;
			myid=0;

			do{myid++;
			}while(myid<parentIds[FPI]);
			myvalue=(int) TBoardReputation(ti.Nowteam);
			if(teamtemp==Tti.Nowteam && teamtemp==ti.Nowteam)
				myvalue+=7000;
			if(deep==0)
				isEnd=true;

//			String space="";
//			for(int i=0;i<Para.SetDeep-deep;i++) {
//				space+="   ";
//			}
//			System.out.println(space+Tti.gameturn+" "+deep+" - P:"+getPointMask(trymask)+" - p:"+parent+" - s: "+getPointMask(FPL)+" -  t:"+enemy(Tti.Nowteam)+" - id:"+myid+" - v:"+myvalue);
		}
		/**
		 * 次世代を作り、Thinksに入れる
		 */
		private void nextgenerations() {
			if(deep>0) {
				for(long mask=1; mask!=0L ; mask<<=1){
					if( (mask & Tti.bitcanboard[Tti.Nowteam])!=0){
						Think t =clone();
						t.Trydo(mask, myid);
						Thinks[FPI].add(t);
					}
				}
			}
		}
		/**
		 * 盤面評価
		 * @param forteam どのチームにとっての評価か
		 * @return 指定チームにおける評価
		 */
		float TBoardReputation(final int forteam){
			float value = 0;
			int enemyteam=enemy(forteam);
			if(isEndGame(Tti)){
					value+=100*Long.bitCount(Tti.bitboard[forteam]);
					value-=100*Long.bitCount(Tti.bitboard[enemyteam]);
					if(getWinner(Tti)==forteam)
						value+=1000*Long.bitCount( ~(Tti.bitboard[Black]|Tti.bitboard[White]) );
					else
						value-=1000*Long.bitCount( ~(Tti.bitboard[Black]|Tti.bitboard[White])) ;
//				System.out.println("endscore");
				isEnd=true;
				return (value);
			}

			value+=Para.Binfo[FPI]*Para.Myp;
			for(long mask=1; mask!=0L ; mask<<=1){
				int intmask=getIntMask(mask);
					if( (Tti.bitcanboard[forteam]&mask)!=0 )
						value+=Para.Binfo[intmask]*Para.CanMe;
					if( (Tti.bitcanboard[enemyteam]&mask)!=0 )
						value-=Para.Binfo[intmask]*Para.CanEn;
					if( (Tti.bitboard[forteam]&mask)!=0 )
						value+=Para.Binfo[intmask]*Para.Bx;
					if( (Tti.bitboard[enemyteam]&mask)!=0 )
						value-=Para.Binfo[intmask]*Para.Bx;
			}
			long cant =getcannotboard();

//			System.out.println(Long.bitCount(cant));
			for(long mask=1; mask!=0L ; mask<<=1)
				if( (cant & mask)!=0)
					if( (Tti.bitboard[forteam]&mask)!=0 )
						value+=Para.Binfo[getIntMask(mask)]*Para.Ne;
					else
						value-=Para.Binfo[getIntMask(mask)]*Para.Ne;
			return(value);
		}
		long getcannotboard() {
			long cannot;
				cannot=nevermovecheck(1L);
				cannot|=nevermovecheck(0b10000000L);
				cannot|=nevermovecheck(0b0000000100000000000000000000000000000000000000000000000000000000L);
				cannot|=nevermovecheck(0b1000000000000000000000000000000000000000000000000000000000000000L);
			return cannot;
		}
		/**
		 * 角からの連続で動かせないかどうかチェックし、can'tに記録
		 * @param start 角mask
		 * @return can't情報配列
		 */
		private long nevermovecheck(final long start) {
			int haveteam;
			if( (start&Tti.bitboard[Black])!=0 )
				haveteam=Black;
			else if( (start&Tti.bitboard[White])!=0 )
				haveteam=White;
			else
				return 0;

			long cant=0,stop,mask=0;
			long cant2=0,stop2,mask2=0;
//			System.out.println(Long.toBinaryString(start));
			switch(getIntMask(start)) {
			case 1:
				stop=start<<7;
				for(long secondstart=start; (secondstart&stop)==0  && secondstart!=0 && (secondstart&Tti.bitboard[haveteam])!=0 ;secondstart<<=8) {
					for(mask=secondstart;  (mask&stop)==0  && (mask&Tti.bitboard[haveteam]) != 0 ; mask<<=1){
						cant|=mask;
						//System.out.println("1  "+getPointMask(mask));
					}
					stop=MPtransfer(mask,7);
				}
				stop2=start<<7*8;
				for(long secondstart=start; (secondstart&stop2)==0  && secondstart!=0 && (secondstart&Tti.bitboard[haveteam])!=0;secondstart<<=1) {
					for(mask2=secondstart;  (mask2&stop2)==0  && (mask2&Tti.bitboard[haveteam]) != 0 ; mask2<<=8){
						cant2|=mask2;
						//System.out.println("2  "+getPointMask(mask));
					}
					stop2=MPtransfer(mask2,-7);
				}
				return cant|cant2;
			case 8:
				stop=start>>>7;
				for(long secondstart=start; (secondstart&stop)==0  && secondstart!=0 && (secondstart&Tti.bitboard[haveteam])!=0;secondstart<<=8) {
					for(mask=secondstart;  (mask&stop)==0  && (mask&Tti.bitboard[haveteam]) != 0 ; mask>>>=1){
						cant|=mask;
						//System.out.println("3  "+getPointMask(mask));
					}
					stop=MPtransfer(mask,9);
				}
				stop2=start<<7*8;
				for(long secondstart=start; (secondstart&stop2)==0  && secondstart!=0 && (secondstart&Tti.bitboard[haveteam])!=0 ;secondstart>>>=1) {
					for(mask2=secondstart;  (mask2&stop2)==0  && (mask2&Tti.bitboard[haveteam]) != 0 ; mask2<<=8){
						cant2|=mask2;
						//System.out.println("4  "+getPointMask(mask));
					}
					stop2=MPtransfer(mask2,-9);
				}
				return cant|cant2;
			case 57:
				stop=start<<7;
				for(long secondstart=start; (secondstart&stop)==0  && secondstart!=0 && (secondstart&Tti.bitboard[haveteam])!=0 ;secondstart>>>=8) {
					for(mask=secondstart;  (mask&stop)==0  && (mask&Tti.bitboard[haveteam]) != 0 ; mask<<=1){
						cant|=mask;
						//System.out.println("5  "+getPointMask(mask));
					}
					stop=MPtransfer(mask,-9);
				}
				stop2=start>>>7*8;
				for(long secondstart=start; (secondstart&stop2)==0  && secondstart!=0 && (secondstart&Tti.bitboard[haveteam])!=0 ;secondstart<<=1) {
					for(mask2=secondstart;  (mask2&stop2)==0  && (mask2&Tti.bitboard[haveteam]) != 0 ; mask2>>>=8){
						cant2|=mask2;
						//System.out.println("6  "+getPointMask(mask));
					}
					stop2=MPtransfer(mask2,9);
				}
				return cant|cant2;
			case 0:
				stop=start>>>7;
				for(long secondstart=start; (secondstart&stop)==0  && secondstart!=0 && (secondstart&Tti.bitboard[haveteam])!=0 ;secondstart>>>=8) {
					for(mask=secondstart;  (mask&stop)==0  && (mask&Tti.bitboard[haveteam]) != 0 ; mask>>>=1){
						cant|=mask;
						//System.out.println("7  "+getPointMask(mask));
					}
					stop=MPtransfer(mask,-7);
				}
				stop2=start>>>7*8;
				for(long secondstart=start; (secondstart&stop2)==0  && secondstart!=0 && (secondstart&Tti.bitboard[haveteam])!=0 ;secondstart>>>=1) {
					for(mask2=secondstart;  (mask2&stop2)==0  && (mask2&Tti.bitboard[haveteam]) != 0 ; mask2>>>=8){
						cant2|=mask2;
						//System.out.println("8  "+getPointMask(mask));
					}
					stop2=MPtransfer(mask2,7);
				}
				return cant|cant2;
			default:
			}
			return cant;
		}
		/**
		 * nevermovecheck用点移動
		 * @param mask mask
		 * @param direction 方向+-7,+-9
		 * @return 移動した点
		 */
		private long MPtransfer(long mask, int direction) {
			switch(direction) {
		    case 7:
		        return (mask << 7) | 0x80808080808080ffL;
		    case -7:
		        return (mask >>> 7) | 0xff01010101010101L;
		    case 9:
		        return (mask << 9) | 0x01010101010101ffL;
		    case -9:
		        return (mask >>> 9) | 0xff80808080808080L;
			default:
				return 0;
			}
		}
		/**
		 * 返信
		 * @return replyvalue 最終評価
		 */
		public int getreplyvalue() {
			return(replyvalue);
		}
	}///////////////*/////////////////////////////////////////////////////////////////
	/**
	 * AIタイマー
	 */
	public class TimerAI extends TimerTask {
		@Override
		public void run() {
			if(!isEndGame(ti))
				AIrun();
		}
	}
	/**
	 * Binfoを読む
	 * @throws IOException 読めたか
	 */
	void loadLearning() throws IOException{
		if(Max.x!=8||Max.y!=8) {
			System.out.println("Invalid Board");
			return;
		}
		File file = new File("LearnResultsSimple.txt");
		FileReader filereader = new FileReader(file);
		int ch;
		int p=1;
		String Inint = "";
		int includeK=0;
		while((ch = filereader.read()) != -1){
			String chst=String.valueOf((char)ch);
			System.out.print(chst);
			if(chst.equals(",")==true){
				Para.Binfo[p]=Integer.parseInt(Inint);
				Inint="";
				p++;
				if(p==64)p=0;
			}else if(chst.equals("E")==true){
				includeK=2;
			}else{
				if(	includeK==0){
					Inint = Inint + chst;
				}else{
					includeK--;
				}
			}
		}
		filereader.close();
		System.out.println();
	}
	/**
	 * ファイルにボードを記録
	 */
	void saveBoard(){
		File file = new File("BitBoradsave.txt");
		try {
			FileWriter filewriter = new FileWriter(file);
			BufferedWriter buffwriter = new BufferedWriter(filewriter);
			buffwriter.write(Long.toString(ti.bitboard[Black]));
			buffwriter.newLine();
			buffwriter.write(Long.toString(ti.bitboard[White]));
			buffwriter.newLine();
			buffwriter.write(Long.toString(ti.bitcanboard[Black]));
			buffwriter.newLine();
			buffwriter.write(Long.toString(ti.bitcanboard[White]));
			buffwriter.newLine();

			buffwriter.write(String.valueOf(ti.Nowteam));
			buffwriter.newLine();

			buffwriter.close();
			filewriter.close();
			System.out.println("saved:board");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * ボードファイルから読み込む
	 */
	void loadBoard(){
		File file = new File("BitBoradsave.txt");
		try {
			FileReader filereader = new FileReader(file);
			BufferedReader buffreader = new BufferedReader(filereader);

			ti.bitboard[Black] = Long.parseLong( buffreader.readLine() );
			ti.bitboard[White] = Long.parseLong( buffreader.readLine() );
			ti.bitcanboard[Black] = Long.parseLong( buffreader.readLine() );
			ti.bitcanboard[White] = Long.parseLong( buffreader.readLine() );

			ti.Nowteam=Integer.parseInt(buffreader.readLine());

			buffreader.close();
			filereader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Clicking=true;
		System.out.println("loaded:board");

		AllChecker(ti);
	}
	void saveCount() {
		File file = new File("PlayStatistics.txt");
		try {
			FileWriter filewriter = new FileWriter(file);
			BufferedWriter buffwriter = new BufferedWriter(filewriter);
			buffwriter.write(Integer.toString(GameCount));
			buffwriter.newLine();
			buffwriter.write(Integer.toString(AIwin[0]));
			buffwriter.newLine();
			buffwriter.write(Integer.toString(AIwin[1]));
			buffwriter.newLine();
			buffwriter.write(Integer.toString(AIwin[BW]));
			buffwriter.newLine();


			buffwriter.close();
			filewriter.close();
			System.out.println("saved:count");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void loadCount() {
		File file = new File("PlayStatistics.txt");
		try {
			FileReader filereader = new FileReader(file);
			BufferedReader buffreader = new BufferedReader(filereader);

			GameCount=Integer.parseInt(buffreader.readLine());
			AIwin[0]=Integer.parseInt(buffreader.readLine());
			AIwin[1]=Integer.parseInt(buffreader.readLine());
			AIwin[BW]=Integer.parseInt(buffreader.readLine());

			buffreader.close();
			filereader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("loaded:count");
	}
	public class mouseListener implements MouseListener {
		@Override
		public void mousePressed(MouseEvent e){
			if(e.getButton()==1&&AIing==false){//左クリック
				long MP=selectpoint(e);
				boolean boolplace=Changer(ti,MP);
				if(boolplace) {

				}else {
					System.out.println("Clicked "+getPointMask(MP) );
				}
				if(boolplace==false&&isEndGame(ti)){//どちらも置けない
					ReversiStart();
					return;
				}else if(isEndGame(ti)){
					return;
				}else if(ti.Nowteam==AIteam){
					AIing=true;
					Timer TimerAIb = new Timer();
					TimerAIb.schedule(new TimerAI(), 500);
				}
			}

			if(e.getButton()==2){

			}

			if(e.getButton()==3&&AIing==false){//右クリック
				boolean fAI=AIing;
				int fteam=ti.Nowteam;

				AIing=true;
				long MP=selectpoint(e);
				Think prep=new Think(MP,0,0);
				long cant=prep.getcannotboard();
				System.out.println("Clicked "+getPointMask(MP)+"  "+getIntMask(MP)+"  "
						+(Long.bitCount(MP&ti.bitboard[Black]))+"-"+(Long.bitCount(MP&ti.bitboard[White]))+"-"
						+(Long.bitCount(MP&ti.bitcanboard[Black]))+"-"+(Long.bitCount(MP&ti.bitcanboard[White]))+"-"+Long.bitCount(MP&cant)
						+"   allcant:"+Long.bitCount(cant)
//						+"   "+Para.Binfo[getIntMask(MP)]+ "     "+Long.toOctalString(MP)
						);

				if( (MP&ti.bitcanboard[Black])!=0) {
					ti.Nowteam=Black;
					Thinks[getIntMask(MP)]=new ArrayList<Think>();
					parentIds[getIntMask(MP)]=0;
					Think thi1 =new Think(MP,Para.SetDeep,0);
					thi1.run();
					System.out.println(teamname(Black)+"  value: "+thi1.getreplyvalue() );
					thi1.getreplyvalue();
				}
				if( (MP&ti.bitcanboard[White])!=0) {
					ti.Nowteam=White;
					Thinks[getIntMask(MP)]=new ArrayList<Think>();
					parentIds[getIntMask(MP)]=0;
					Think thi2 =new Think(MP,Para.SetDeep,0);
					thi2.run();
					System.out.println(teamname(White)+"  value: "+thi2.getreplyvalue() );
				}

				ti.Nowteam=fteam;
				AIing=fAI;

				AllChecker(ti);
			}
		}

		long selectpoint(MouseEvent e){
				Component com = e.getComponent();
				for(int Y=0 ;Y<Max.y;Y++){
					for(int X=0 ;X<Max.x;X++){
						if(com==RG.jpc[X][Y]) {
							return(getMask(X,Y));
						}
					}
				}
				System.err.println("null");
					return 0;
		}
		@Override
		public void mouseClicked(MouseEvent e) {
		}
		@Override
		public void mouseReleased(MouseEvent e) {
		}
		@Override
		public void mouseEntered(MouseEvent e) {
		}
		@Override
		public void mouseExited(MouseEvent e) {
		}
	}
	public class windowcomp implements ComponentListener{
		@Override
		public void componentResized(ComponentEvent e) {
			Dimension wp =RG.getSize();
			int wx = (int) wp.getWidth()-10;
			int wy = (int) wp.getHeight()-40;
			if(wx>wy) {
				imgsize=wy/(Max.y);
			}else {
				imgsize=wx/(Max.x);
			}

			RG.Resize=true;
//			System.out.println("Resize");
		}
		@Override
		public void componentMoved(ComponentEvent e) {
//			System.out.println("Move");
		}
		@Override
		public void componentShown(ComponentEvent e) {
//			System.out.println("Shown");
		}
		@Override
		public void componentHidden(ComponentEvent e) {
//			System.out.println("Hide");
		}
	}
}
