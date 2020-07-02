package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;
import static br.ce.wcaquino.utils.DataUtils.verificarDiaSemana;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService {
	
	private LocacaoDAO dao;
	private SPCService spc;
	private EmailService emailService;
	
	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {
		if(usuario == null) {
			throw new LocadoraException("Usuario vazio");
		}
		
		if(filmes == null || filmes.isEmpty()) {
			throw new LocadoraException("Filme vazio");
		}
		
		List<Filme> filmesSemEstoque = filmes.stream().filter(f -> f.getEstoque() == 0).collect(Collectors.toList());
		if(filmesSemEstoque.size() >= 1) {
			throw new FilmeSemEstoqueException();
		}
		
		if(spc.possuiNegativacao(usuario)) {
			throw new LocadoraException("Usuário negativado");
		}
		
		Locacao locacao = new Locacao();
		locacao.setFilme(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		double valor = 0.0;
		for(int i = 0; i < filmes.size(); i++) {
			Filme filme = filmes.get(i);
			double valorFilme = filme.getPrecoLocacao();
			
			switch(i) {
				case 2:
					valorFilme = valorFilme * 0.75;
					break;
				case 3:
					valorFilme = valorFilme * 0.5;
					break;
				case 4:
					valorFilme = valorFilme * 0.25;
					break;
				case 5: 
					valorFilme = 0;
					break;
			}
		
			valor += valorFilme;
		}
		locacao.setValor(valor);

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if(verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			DataUtils.adicionarDias(dataEntrega, 1);
		}
		
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		dao.salvar(locacao);
		
		return locacao;
	}
	
	public void notificarAtraso() {
		List<Locacao> locacoes = dao.obterLocacoesPendentes();
		for(Locacao locacao : locacoes) {
			if(locacao.getDataRetorno().before(new Date())) {
				emailService.notificarAtraso(locacao.getUsuario());
			}
		}
	}
	
	
	public void setLocacaoDao(LocacaoDAO dao) {
		this.dao = dao;
	}
	
	public void setSpcService(SPCService spc) {
		this.spc = spc;
	}
	
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
}