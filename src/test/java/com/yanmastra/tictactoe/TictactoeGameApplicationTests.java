package com.yanmastra.tictactoe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TictactoeGameApplicationTests {
	private Logger log = LogManager.getLogger(TictactoeGameApplicationTests.class);

	@Test
	void contextLoads() {
		GameUtils gameUtils = new GameUtils();
		gameUtils.loop(5, new GameUtils.LooperBoard() {
			@Override
			public boolean onPosition(int x, int y, int number) {
				log.info("x="+x+", y="+y+", number="+number);
				return true;
			}
		});
	}

}
