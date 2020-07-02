package br.ce.wcaquino.matchers;

import static java.util.Calendar.MONDAY;

public class MatchersProprios {
	
	public static DiaSemanaMatcher caiEm(Integer diaSemana) {
		return new DiaSemanaMatcher(diaSemana);
	}
	
	public static DiaSemanaMatcher caiNumaSegunda() {
		return new DiaSemanaMatcher(MONDAY);
	}
	
	public static DataDiferencaDiasMatcher ehAmanha() {
		return new DataDiferencaDiasMatcher(1);
	}
	
	public static DataDiferencaDiasMatcher ehHoje() {
		return new DataDiferencaDiasMatcher(0);
	}

}
