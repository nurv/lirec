PDK drive system example

This readme is intended to provide a high-level overview of the very simple example presented to demonstrate Pleo's drive system.

In this example, when Pleo wakes up, his blood sugar and happiness are at a certain level, but they are each dropping as time goes by.  This sample application monitors his blood sugar and happiness levels and uses them to select an active drive.

drive_example Pleo has two competing drives - his social drive and his hunger drive.  By default, Pleo is social (i.e. his social drive is active), but if his blood sugar drops below a certain level, then his hunger drive is activated.

drive_example Pleo has two distinct behaviors when he is social.  When his happiness level is high, he wags and makes happy panting noises.  When his happiness level is low, he whines.  Touching Pleo somewhere on his body makes Pleo happier.  If he's not touched, his happiness level drops.

When Pleo is hungry, he has only one behavior - he sniffs around for food and attempts to eat things off the ground.  If he gets something in his mouth, he eats it, and his blood sugar rises.