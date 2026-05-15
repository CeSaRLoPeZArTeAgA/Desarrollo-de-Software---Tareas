const form = document.querySelector("#incidentForm");
const formMessage = document.querySelector("#formMessage");
const incidentList = document.querySelector("#incidentList");
const reloadButton = document.querySelector("#reloadButton");
const filterTipo = document.querySelector("#filterTipo");
const filterEstado = document.querySelector("#filterEstado");

function showMessage(text, type) {
  formMessage.textContent = text;
  formMessage.className = `message ${type}`;
}

function formatTipo(tipo) {
  const names = {
    ALUMBRADO_PUBLICO: "Alumbrado público",
    BASURA: "Basura",
    SEGURIDAD_CIUDADANA: "Seguridad ciudadana",
    EMERGENCIA: "Emergencia"
  };

  return names[tipo] || tipo;
}

function formatEstado(estado) {
  const names = {
    REGISTRADO: "Registrado",
    VALIDADO: "Validado",
    EN_ATENCION: "En atención",
    RESUELTO: "Resuelto",
    RECHAZADO: "Rechazado"
  };

  return names[estado] || estado;
}

async function loadIncidents() {
  const params = new URLSearchParams();

  if (filterTipo.value) {
    params.set("tipo", filterTipo.value);
  }

  if (filterEstado.value) {
    params.set("estado", filterEstado.value);
  }

  const response = await fetch(`/api/incidentes?${params.toString()}`);
  const result = await response.json();

  if (!result.ok) {
    incidentList.innerHTML = `<p>No se pudieron cargar las incidencias.</p>`;
    return;
  }

  if (result.data.length === 0) {
    incidentList.innerHTML = `<p>No hay incidencias registradas.</p>`;
    return;
  }

  incidentList.innerHTML = result.data
    .map((incident) => {
      const urgentClass = incident.prioridad === "URGENTE" ? "urgent" : "";

      return `
        <article class="incident">
          <h3>#${incident.id} - ${incident.titulo}</h3>

          <div class="badges">
            <span class="badge">${formatTipo(incident.tipo)}</span>
            <span class="badge">${formatEstado(incident.estado)}</span>
            <span class="badge ${urgentClass}">${incident.prioridad}</span>
          </div>

          <p>${incident.descripcion}</p>
          <p><strong>Dirección:</strong> ${incident.direccion}, ${incident.distrito}</p>
          <p><strong>Creado:</strong> ${new Date(incident.createdAt).toLocaleString()}</p>
        </article>
      `;
    })
    .join("");
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const formData = new FormData(form);

  try {
    const response = await fetch("/api/incidentes/multipart", {
      method: "POST",
      body: formData
    });

    const result = await response.json();

    if (!result.ok) {
      showMessage(result.error || "No se pudo registrar la incidencia.", "error");
      return;
    }

    showMessage(`Incidencia registrada con ID ${result.data.id}.`, "ok");
    form.reset();
    await loadIncidents();
  } catch (error) {
    showMessage("Error de conexión con el servidor.", "error");
  }
});

reloadButton.addEventListener("click", loadIncidents);
filterTipo.addEventListener("change", loadIncidents);
filterEstado.addEventListener("change", loadIncidents);

loadIncidents();
