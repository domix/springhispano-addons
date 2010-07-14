 _____            _             _    _ _                             
 / ____|          (_)           | |  | (_)                            
| (___  _ __  _ __ _ _ __   __ _| |__| |_ ___ _ __   __ _ _ __   ___  
 \___ \| '_ \| '__| | '_ \ / _` |  __  | / __| '_ \ / _` | '_ \ / _ \ 
 ____) | |_) | |  | | | | | (_| | |  | | \__ \ |_) | (_| | | | | (_) |
|_____/| .__/|_|  |_|_| |_|\__, |_|  |_|_|___/ .__/ \__,_|_| |_|\___/ 
       | |                  __/ |            | |                      
       |_|                 |___/             |_|
	            _     _                       
	    /\      | |   | |                      
	   /  \   __| | __| |______ ___  _ __  ___ 
	  / /\ \ / _` |/ _` |______/ _ \| '_ \/ __|
	 / ____ \ (_| | (_| |     | (_) | | | \__ \
	/_/    \_\__,_|\__,_|      \___/|_| |_|___/

====================================================================================================
INSTRUCCIONES
==================================================================================================== 

TBD         

====================================================================================================
ECLIPSE
====================================================================================================
   
Para generar los archivos necesarios para abrir el proyecto con eclipse hay que ejecutar:

> mvn eclipse:eclipse

NOTA: Deben tener paciencia ya que maven bajara muchos jars y source.jars

luego importar el proyecto en eclipse:

Import... -> General/Existing Projects into Workspace

Seleccionar la ruta del proyecto, dar click a Finish y listo!

====================================================================================================
REPOSITORIO DE CODIGO
==================================================================================================== 

No se vale subir al repositorio lo siguiente:

1. Los archivos de proyecto de Eclipse (.project, .classpath, etc.)
2. Los folder target que genera Maven (donde van los .class, etc.)
3. TBD

====================================================================================================
TIPS DE MERCURIAL
====================================================================================================

Para actualizar el proyecto:

> hg pull

Para dar commit de los cambios hechos:

> hg commit <ruta> -m "<Mensaje del commit>"

NOTA: La primera vez deben especificar el usuario con el cual dan commit usando el argumento --user
y les va a pedir su password de google code.

Para subir los cambios al repositorio despues de haber hecho commit local:

> hg push