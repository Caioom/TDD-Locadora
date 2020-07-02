package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilmeSemEstoque;
import static br.ce.wcaquino.builders.LocacaoBuilder.umLocacao;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static br.ce.wcaquino.matchers.MatchersProprios.ehAmanha;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.utils.DataUtils.obterDataComDiferencaDias;
import static br.ce.wcaquino.utils.DataUtils.verificarDiaSemana;
import static java.util.Arrays.asList;
import static java.util.Calendar.SATURDAY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import junit.framework.Assert;

public class LocacaoServiceTest {
	
	private LocacaoService service;
	
	private SPCService spc;
	private LocacaoDAO dao;
	private EmailService email;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule
	public ExpectedException exception = none();
	
	@Before
	public void setup() {
		service = new LocacaoService();
		dao = Mockito.mock(LocacaoDAO.class);
		service.setLocacaoDao(dao);
		spc = Mockito.mock(SPCService.class);
		service.setSpcService(spc);
		email = Mockito.mock(EmailService.class);
		service.setEmailService(email);
	}
	
	@Test
	public void deveAlugarFilme() throws Exception {
		assumeFalse(verificarDiaSemana(new Date(), SATURDAY));
		
		//Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = asList(umFilme().comValor(20.5).agora());
		
		//Acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
		 
		//Verificacao
		error.checkThat(locacao.getValor(), is(equalTo(20.5)));
		error.checkThat(locacao.getDataLocacao(), ehHoje());
		error.checkThat(locacao.getDataRetorno(), ehAmanha());
		
	}
	
	@Test(expected = FilmeSemEstoqueException.class)
	public void deveLancarExcecaoComFilmeSemEstoque() throws Exception {
		//Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = asList(umFilmeSemEstoque().agora());
		
		//Acao
		service.alugarFilme(usuario, filmes);
	}
	
	@Test
	public void deveLancarExcecaoSeAlugarFilmeComUsuarioVazio() throws FilmeSemEstoqueException {
		//Cenario
		List<Filme> filmes = asList(umFilme().agora());
		
		try {
			service.alugarFilme(null, filmes);
			fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuario vazio"));
		}
	}
	
	@Test 
	public void deveLancarExcecaoAoAlugarFilmeVazio() throws FilmeSemEstoqueException, LocadoraException {
		//Cenario
		Usuario usuario = umUsuario().agora();
		
		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme vazio");
		
		service.alugarFilme(usuario, null);
	}
	
	
	@Test
	public void deveDevolverSegundaSeFilmeAlugadoNoSabado() throws FilmeSemEstoqueException, LocadoraException {
		assumeTrue(verificarDiaSemana(new Date(), SATURDAY));
		
		//cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = asList(umFilme().agora());
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filmes);
		
		//verificacao
		assertThat(resultado.getDataRetorno(), caiNumaSegunda());
	}
	
	@Test
	public void naoDeveAlugarFilmesParaUsuarioNegativadoSPC() throws FilmeSemEstoqueException {
		//Cenario 
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = asList(umFilme().agora());
		
		when(spc.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);
		
		
		//Acao
		try {
			service.alugarFilme(usuario, filmes);
			//verificacao
			Assert.fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuário negativado"));
		}
		
		verify(spc).possuiNegativacao(usuario);
	}
	
	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
		//cenario
		Usuario usuario = umUsuario().agora();
		Usuario usuario2 = umUsuario().comNome("Usuario em dia").agora();
		Usuario usuario3 = umUsuario().comNome("Usuario em atraso").agora();
		List<Locacao> locacoes = 
				asList(
						umLocacao().comUsuario(usuario).comAtraso().agora(),
						umLocacao().comUsuario(usuario2).agora(),
						umLocacao().comUsuario(usuario3).comAtraso().agora(),
						umLocacao().comUsuario(usuario3).comAtraso().agora());
		
		when(dao.obterLocacoesPendentes()).thenReturn(locacoes);
		
		//acao
		service.notificarAtraso();
		
		//verificacao
		verify(email, times(3)).notificarAtraso(Mockito.any(Usuario.class));
		verify(email).notificarAtraso(usuario);
		verify(email, atLeastOnce()).notificarAtraso(usuario3);
		verify(email, never()).notificarAtraso(usuario2);
		verifyNoMoreInteractions(email);
	}
}
