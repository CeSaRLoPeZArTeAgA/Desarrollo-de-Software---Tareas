export interface DocumentoEntidad {
  readonly id: number;
  readonly dependencia: string;
  readonly nro_documento: string;
  readonly estado_documento: string;
  readonly fecha_tramite: string;
  readonly fecha_respuesta: string;
  readonly contenedor: string;
}

export interface VerificacionFirmaResult {
  readonly tramite_id: number;
  readonly nro_documento: string;
  readonly verificacion_correcta: boolean;
  readonly mensaje: string;
  readonly detalle?: unknown;
}

const DEFAULT_API_BASE_URL = "http://127.0.0.1:8000";

const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim() ||
  DEFAULT_API_BASE_URL;

function buildUrl(path: string): string {
  return `${API_BASE_URL}${path}`;
}

async function readError(response: Response): Promise<string> {
  const text = await response.text();

  if (!text) {
    return "No se pudo completar la operación.";
  }

  try {
    const json = JSON.parse(text) as { detail?: string };
    return json.detail || text;
  } catch {
    return text;
  }
}

export async function listarDocumentosEntidad(
  dependencia: string,
): Promise<readonly DocumentoEntidad[]> {
  const query = new URLSearchParams({ dependencia });

  const response = await fetch(
    buildUrl(`/api/entidad-simulada/documentos?${query.toString()}`),
  );

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return (await response.json()) as readonly DocumentoEntidad[];
}

export async function verificarFirmaEntidad(
  tramiteId: number,
): Promise<VerificacionFirmaResult> {
  const response = await fetch(
    buildUrl(`/api/entidad-simulada/documentos/${tramiteId}/verificar`),
    {
      method: "POST",
    },
  );

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return (await response.json()) as VerificacionFirmaResult;
}

export async function borrarDocumentoEntidad(tramiteId: number): Promise<void> {
  const response = await fetch(
    buildUrl(`/api/entidad-simulada/documentos/${tramiteId}`),
    {
      method: "DELETE",
    },
  );

  if (!response.ok) {
    throw new Error(await readError(response));
  }
}

export function buildDescargaPaqueteEntidadUrl(tramiteId: number): string {
  return buildUrl(`/api/entidad-simulada/documentos/${tramiteId}/download-paquete`);
}
