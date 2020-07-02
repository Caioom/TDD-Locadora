package br.ce.wcaquino.matchers;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;
import static br.ce.wcaquino.utils.DataUtils.isMesmaData;

import java.util.Date;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import br.ce.wcaquino.utils.DataUtils;

public class DataDiferencaDiasMatcher extends TypeSafeMatcher<Date> {
	
	private Integer qtdDiasDiferenca;
	
	public DataDiferencaDiasMatcher(Integer qtdDiasDiferenca) {
		this.qtdDiasDiferenca = qtdDiasDiferenca;
	}

	@Override
	public void describeTo(Description description) {
		Date dataRetorno = adicionarDias(new Date(), qtdDiasDiferenca);
		description.appendValue(dataRetorno);
	}

	@Override
	protected boolean matchesSafely(Date dataRetorno) {
		return isMesmaData(dataRetorno, DataUtils.obterDataComDiferencaDias(qtdDiasDiferenca));
	}

}
