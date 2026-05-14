---
trigger: model_decision
description: Tareas del proyecto en android tv
---

We need to add the vertical mode at the menu in order to rotate the Splash Screen and the Constant input screen like we do with the images when these are vertical, we need to save that value in order to have it activated or not when we init the app or go back from the images, do the same thing as we do in this Modifier of the images to rotate in a correct way the screen and dont have size issues:

Modifier.fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = 90f
                                    scaleX = maxHeight.value / maxWidth.value // this 2 lines are important
                                    scaleY = maxWidth.value / maxHeight.value }
                                
                    