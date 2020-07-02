package br.ce.wcaquino.servicos;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import br.ce.wcaquino.exceptions.NaoPodeDividirPorZeroException;

public class CalculadoraTest {
	
	private Calculadora calc;
	
	@Before
	public void setup() {
		this.calc = new Calculadora();
	}

	@Test
	public void deveSomarDoisValores() {
		//cenario 
		int a = 5;
		int b = 3;
		
		//acao 
		int result = calc.somar(a, b);
		
		//verificacao
		assertThat(result, is(8));
	}
	
	@Test
	public void deveSubtrairDoisNumeros() {
		//cenario 
		int a = 8;
		int b = 5;
		
		//acao
		int result = calc.subtrair(a, b);
		
		//verificacao
		assertThat(result, is(3));
	}
	
	@Test
	public void deveDividirDoisValores() throws NaoPodeDividirPorZeroException {
		//cenario
		int a = 6;
		int b = 3;
		
		//acao
		int result = calc.dividir(a, b);
		
		//verificacao
		assertThat(result, is(2));
	}
	
	@Test(expected = NaoPodeDividirPorZeroException.class)
	public void naoPodeDividirPorZero() throws NaoPodeDividirPorZeroException {
		//cenario
		int a = 10;
		int b = 0;
		
		//acao
		calc.dividir(a, b);
	}
}
