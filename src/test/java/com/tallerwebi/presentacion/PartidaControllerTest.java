package com.tallerwebi.presentacion;

import com.tallerwebi.controller.PartidaController;
import com.tallerwebi.dominio.enums.TIPO_PARTIDA;
import com.tallerwebi.model.Partida;
import com.tallerwebi.model.Usuario;
import com.tallerwebi.service.ServicioPartida;
import com.tallerwebi.service.impl.ServicioUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class PartidaControllerTest {

    private ServicioPartida servicioPartida;
    private ServicioUsuario servicioUsuario;
    private final SimpMessagingTemplate messagingTemplate;

    public PartidaControllerTest() {
        servicioPartida = mock(ServicioPartida.class);
        servicioUsuario = mock(ServicioUsuario.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
    }

    @Test
    public void siElUsuarioEstaLogeado_seCargaLaPartida() {
        HttpServletRequest request = givenUsuarioEnSesion();

        ModelAndView mav = whenCargarPartida(request, TIPO_PARTIDA.SUPERVIVENCIA);
        thenSeCargaLaVistaConElJugador(mav);
    }

    private HttpServletRequest givenUsuarioEnSesion() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        Usuario jugador = new Usuario("Nicolas127", "nico@caba.com", "123456");
        jugador.setId(1L);  // Asignale un ID también

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("USUARIO")).thenReturn(jugador);

        return request;
    }

    private ModelAndView whenCargarPartida(HttpServletRequest request, TIPO_PARTIDA tipo) {
        // Creamos un objeto Partida mock con id para evitar nullpointer
        Partida partidaMock = new Partida();
        partidaMock.setId(100L);

        // Configuramos el mock para que devuelva la partida cuando se llame al método
        when(servicioPartida.crearOUnirsePartida(any(Usuario.class), eq(tipo))).thenReturn(partidaMock);

        // Configuramos el mock para el avatar
        when(servicioUsuario.obtenerImagenAvatarSeleccionado(anyLong())).thenReturn("avatar.png");

        PartidaController partidaController = new PartidaController(servicioPartida, servicioUsuario, messagingTemplate);
        return partidaController.cargarPartida(request, tipo);
    }

    private void thenSeCargaLaVistaConElJugador(ModelAndView mav) {
        assertEquals("cargarPartida", mav.getViewName());

        Usuario jugador = (Usuario) mav.getModel().get("jugador");
        assertEquals("Nicolas127", jugador.getNombreUsuario());

        Partida partida = (Partida) mav.getModel().get("partida");
        assertNotNull(partida);
        assertEquals(100L, partida.getId().longValue());

        String avatarImg = (String) mav.getModel().get("avatarImg");
        assertEquals("avatar.png", avatarImg);
    }
}
