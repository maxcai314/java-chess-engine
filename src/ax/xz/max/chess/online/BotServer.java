package ax.xz.max.chess.online;

public class BotServer {

	public static void main(String[] args) throws InterruptedException {
		var server = new LichessBotServer(ClientProfile.initialize());
		Thread.sleep(Long.MAX_VALUE);
	}
}
