# Flujo de trabajo GitFlow

## 1. Ramas principales

```text
main
develop
```

## main

Contiene versiones estables del sistema.

## develop

Contiene la integración del desarrollo actual.

---

# 2. Ramas de funcionalidades

Cada funcionalidad se implementa en una rama `feature`.

Ejemplos:

```text
feature/registro-incidencias
feature/subida-evidencias
feature/historial-estados
feature/frontend-ciudadano
```

---

# 3. Ramas de release

Cuando el sistema está listo para entregar:

```text
release/v1.0.0
```

---

# 4. Ramas hotfix

Para correcciones urgentes:

```text
hotfix/corregir-validacion-emergencia
```

---

# 5. Comandos sugeridos

## Crear develop

```bash
git checkout -b develop
```

## Crear feature

```bash
git checkout develop
git checkout -b feature/registro-incidencias
```

## Guardar cambios

```bash
git add .
git commit -m "feat: implementar registro de incidencias"
```

## Integrar feature a develop

```bash
git checkout develop
git merge feature/registro-incidencias
```

## Crear release

```bash
git checkout -b release/v1.0.0
```

## Pasar release a main

```bash
git checkout main
git merge release/v1.0.0
git tag v1.0.0
```

---

# 6. Convención de commits

```text
feat: nueva funcionalidad
fix: corrección de error
docs: documentación
test: pruebas
refactor: mejora interna del código
style: formato visual
```

Ejemplos:

```bash
git commit -m "feat: agregar endpoint para registrar incidencias"
git commit -m "docs: documentar casos de uso"
git commit -m "test: agregar pruebas de cambio de estado"
```
