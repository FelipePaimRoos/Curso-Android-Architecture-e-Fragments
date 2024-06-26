package br.com.alura.technews.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import br.com.alura.technews.R
import br.com.alura.technews.database.AppDatabase
import br.com.alura.technews.model.Noticia
import br.com.alura.technews.repository.NoticiaRepository
import br.com.alura.technews.ui.activity.NOTICIA_ID_CHAVE
import br.com.alura.technews.ui.activity.extensions.mostraErro
import br.com.alura.technews.ui.fragment.extensions.mostraErro
import br.com.alura.technews.ui.viewmodel.VisualizaNoticiaViewModel
import br.com.alura.technews.ui.viewmodel.factory.VisualizaNoticiaViewModelFactory
import kotlinx.android.synthetic.main.visualiza_noticia.visualiza_noticia_texto
import kotlinx.android.synthetic.main.visualiza_noticia.visualiza_noticia_titulo

private const val MENSAGEM_FALHA_REMOCAO = "Não foi possível remover notícia"
private const val NOTICIA_NAO_ENCONTRADA = "Notícia não encontrada"
private const val TITULO_APPBAR = "Notícias"
class VisualizaNoticiaFragment: Fragment() {

    private val noticiaId: Long by lazy {
         arguments?.getLong(NOTICIA_ID_CHAVE) ?: throw IllegalArgumentException("ID inválido")
    }

    private val viewModel by lazy {
        val repository = NoticiaRepository(AppDatabase.getInstance(context!!).noticiaDAO)
        val factory = VisualizaNoticiaViewModelFactory(noticiaId, repository)
        ViewModelProviders.of(this, factory).get(VisualizaNoticiaViewModel::class.java)
    }

    var  quandoSelecionaMenuEdicao: (noticia: Noticia) -> Unit = {  }
    var quandoFinalizaTela: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        verificaIdDaNoticia()
        buscaNoticiaSelecionada()

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.visualiza_noticia_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.visualiza_noticia_menu_edita -> {
                viewModel.noticiaEncontrada.value?.let(quandoSelecionaMenuEdicao)
            }
            R.id.visualiza_noticia_menu_remove -> remove()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.visualiza_noticia, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = TITULO_APPBAR
    }

    private fun buscaNoticiaSelecionada() {
        viewModel.noticiaEncontrada.observe(this, Observer { noticiaEncontrada ->
            noticiaEncontrada?.let {
                preencheCampos(it)
            }
        })
    }

    private fun verificaIdDaNoticia() {
        if (noticiaId == 0L) {
            mostraErro(NOTICIA_NAO_ENCONTRADA)
            quandoFinalizaTela()
        }
    }

    private fun preencheCampos(noticia: Noticia) {
        visualiza_noticia_titulo.text = noticia.titulo
        visualiza_noticia_texto.text = noticia.texto
    }

    private fun remove() {
        viewModel.remove().observe(this, Observer {
            if (it.erro == null) {
                quandoFinalizaTela()
            } else {
                mostraErro(MENSAGEM_FALHA_REMOCAO)
            }
        })
    }

}