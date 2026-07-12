import { useEffect, useMemo, useState } from "react";

import { listarDependencias, type DependenciaDb } from "../api/dependenciasApi";
import {
  borrarDocumentoEntidad,
  buildDescargaPaqueteEntidadUrl,
  listarDocumentosEntidad,
  verificarFirmaEntidad,
  type DocumentoEntidad,
} from "../api/entidadSimuladaApi";

import "../stylesEntidadSimulada.css";

export function EntidadSimuladaView(): JSX.Element {
  const [dependencias, setDependencias] = useState<readonly DependenciaDb[]>([]);
  const [dependenciaSeleccionada, setDependenciaSeleccionada] = useState("");
  const [documentos, setDocumentos] = useState<readonly DocumentoEntidad[]>([]);
  const [documentoSeleccionadoId, setDocumentoSeleccionadoId] = useState<number | null>(
    null,
  );
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const documentoSeleccionado = useMemo(
    () => documentos.find((documento) => documento.id === documentoSeleccionadoId) ?? null,
    [documentos, documentoSeleccionadoId],
  );

  useEffect(() => {
    let mounted = true;

    async function cargarDependencias(): Promise<void> {
      try {
        const data = await listarDependencias();

        if (mounted) {
          setDependencias(data);
        }
      } catch (err) {
        if (mounted) {
          setError(err instanceof Error ? err.message : "No se pudieron cargar las dependencias.");
        }
      }
    }

    void cargarDependencias();

    return () => {
      mounted = false;
    };
  }, []);

  async function cargarDocumentosPorDependencia(dependencia: string): Promise<void> {
    setCargando(true);
    setMensaje(null);
    setError(null);
    setDocumentoSeleccionadoId(null);

    try {
      const data = await listarDocumentosEntidad(dependencia);
      setDocumentos(data);
    } catch (err) {
      setDocumentos([]);
      setError(err instanceof Error ? err.message : "No se pudieron cargar los documentos.");
    } finally {
      setCargando(false);
    }
  }

  async function handleDependenciaChange(
    event: React.ChangeEvent<HTMLSelectElement>,
  ): Promise<void> {
    const dependencia = event.target.value;
    setDependenciaSeleccionada(dependencia);

    if (!dependencia) {
      setDocumentos([]);
      setDocumentoSeleccionadoId(null);
      setMensaje(null);
      setError(null);
      return;
    }

    await cargarDocumentosPorDependencia(dependencia);
  }

  async function handleVerificarFirma(): Promise<void> {
    if (!documentoSeleccionado) {
      return;
    }

    setCargando(true);
    setMensaje(null);
    setError(null);

    try {
      const result = await verificarFirmaEntidad(documentoSeleccionado.id);

      setMensaje(
        result.verificacion_correcta
          ? `Verificación correcta para el documento ${result.nro_documento}.`
          : `La verificación no fue satisfactoria para el documento ${result.nro_documento}.`,
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo verificar la firma.");
    } finally {
      setCargando(false);
    }
  }

  function handleDescargarDocumento(): void {
    if (!documentoSeleccionado) {
      return;
    }

    window.open(
      buildDescargaPaqueteEntidadUrl(documentoSeleccionado.id),
      "_blank",
      "noopener,noreferrer",
    );
  }

  async function handleBorrarDocumento(): Promise<void> {
    if (!documentoSeleccionado) {
      return;
    }

    const confirmed = window.confirm(
      `¿Deseas borrar el documento ${documentoSeleccionado.nro_documento}?`,
    );

    if (!confirmed) {
      return;
    }

    setCargando(true);
    setMensaje(null);
    setError(null);

    try {
      await borrarDocumentoEntidad(documentoSeleccionado.id);

      setDocumentos((current) =>
        current.filter((documento) => documento.id !== documentoSeleccionado.id),
      );
      setDocumentoSeleccionadoId(null);
      setMensaje("Documento borrado correctamente.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "No se pudo borrar el documento.");
    } finally {
      setCargando(false);
    }
  }

  return (
    <main className="entidad-page">
      <section className="entidad-card">
        <div className="entidad-review-box">
          <label className="entidad-label" htmlFor="dependencia-revisora">
            Dependencia Revisora
          </label>

          <select
            id="dependencia-revisora"
            value={dependenciaSeleccionada}
            onChange={(event) => void handleDependenciaChange(event)}
            className="entidad-select"
          >
            <option value="">Seleccione dependencia a revisar</option>
            {dependencias.map((dependencia) => (
              <option key={dependencia.id} value={dependencia.nombre}>
                {dependencia.nombre}
              </option>
            ))}
          </select>
        </div>

        <div className="entidad-search-title">
          <h2>Busqueda de documentos tramitados</h2>
          <p>visualizar documentos tramitados.</p>
        </div>

        <div className="entidad-table-wrapper">
          <table className="entidad-table">
            <thead>
              <tr>
                <th>Dependencia</th>
                <th>Nro de Documento</th>
                <th>Estado Documento</th>
                <th>Fecha Tramite</th>
                <th>Fecha Respuesta</th>
              </tr>
            </thead>

            <tbody>
              {documentos.length === 0 ? (
                <tr>
                  <td className="entidad-empty" colSpan={5}>
                    {dependenciaSeleccionada
                      ? "No hay documentos tramitados para esta dependencia."
                      : "Seleccione una dependencia para cargar documentos."}
                  </td>
                </tr>
              ) : (
                documentos.map((documento) => (
                  <tr
                    key={documento.id}
                    className={
                      documento.id === documentoSeleccionadoId
                        ? "entidad-row entidad-row-selected"
                        : "entidad-row"
                    }
                    onClick={() => setDocumentoSeleccionadoId(documento.id)}
                  >
                    <td>{documento.dependencia}</td>
                    <td>{documento.nro_documento}</td>
                    <td>{documento.estado_documento}</td>
                    <td>{documento.fecha_tramite}</td>
                    <td>{documento.fecha_respuesta}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {documentoSeleccionado ? (
          <p className="entidad-selected-info">
            Documento seleccionado: <strong>{documentoSeleccionado.nro_documento}</strong>
          </p>
        ) : null}

        {error ? <div className="entidad-message entidad-message-error">{error}</div> : null}
        {mensaje ? <div className="entidad-message entidad-message-ok">{mensaje}</div> : null}

        <div className="entidad-actions">
          <button
            type="button"
            className="entidad-action-button"
            onClick={() => void handleVerificarFirma()}
            disabled={!documentoSeleccionado || cargando}
          >
            Verificacion Firma
          </button>

          <button
            type="button"
            className="entidad-action-button"
            onClick={handleDescargarDocumento}
            disabled={!documentoSeleccionado || cargando}
          >
            Descarga Documento
          </button>

          <button
            type="button"
            className="entidad-action-button"
            onClick={() => void handleBorrarDocumento()}
            disabled={!documentoSeleccionado || cargando}
          >
            Borrar Documento
          </button>
        </div>
      </section>
    </main>
  );
}
