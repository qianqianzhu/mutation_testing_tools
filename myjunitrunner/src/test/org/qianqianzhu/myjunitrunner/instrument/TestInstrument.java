package org.qianqianzhu.myjunitrunner.instrument;

import org.qianqianzhu.myjunitrunner.example.Lion;

public class TestInstrument {
	public static void main(String args[]) throws InterruptedException {
		Lion l = new Lion();
		l.runLion();
	}
}