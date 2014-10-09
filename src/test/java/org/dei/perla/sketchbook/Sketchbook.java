package org.dei.perla.sketchbook;

public class Sketchbook {

	public static void main(String args[]) {
		System.out.println("test".matches("param\\['*'\\]"));
		System.out.println("param['ciccio']".matches("param\\['.*'\\]"));
	}

}
