---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Cambios a realizar en la app:

1. Priorizamos mostrar datos en local antes que descargarlos SIEMPRE que se inicia la app, solo si una vez iniciada la app de manera local se comprueba que hay cambios en el json de sync cargamos mediante la web los datos nuevos.

2. Al estar en modo vertical, la pantalla está experimentando una refactorización de tamaño en todo, tanto imagenes como texto (por ejemplo del texto de la splash screen), estaría bien que ese texto no aparezca mal formateado sino con su tamaño adecuado (revisar lógica de girado de pantalla para que se vea más profesional y atractivo)

