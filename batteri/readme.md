# Batteri

L'obiettivo di ogni gruppo è quello di creare il batterio più forte sul campo, ogni batterio creato deve essere una classe che estende la classe astratta main.Batterio

## Regole

Si definisce un limite massimo in ns (nano secondi) che ogni batterio può impiegare nel suo metodo move(), se un batterio supera questo limite viene squalificato perchè troppo esoso di tempo

(La misurazione non è sempre precisa al 100% perciò si possono verificare misurazioni errate da entrambi i lati)

Questa è l'unica regola che è necessario valutare manualmente, tutte le altre sono scritte nel codice dell'arena e non possono venire superate

## Vittoria

Il vincitore sarà decretato al termine della simulazione come l'unico batterio sopravvissuto od il batterio sopravvissuto con quantità maggiori tra quelli ancora in vita.

## Metodi

Il metodo principale è **move()**, dentro di esso bisogna implementare la strategia migliore affinché raggiunga il cibo prima degli avversari, cosi da sopravvivere più a lungo e riprodursi

I metodi secondari sono **il costruttore** e **clone()** che possono (o meno) implementare strategie più efficienti per essere i più competitivi possibile

## Consigli

Nel costruttore si può definire la posizione iniziale dei batteri

La gara viene fatta utilizzando la distribuzione "square" quindi è l'unica su cui bisogna focalizzarsi

Si possono utilizzare i metodi (statici) delle classi:

- main.Batterio

- main.Food

- main.mainForm

per avere più dati sull'arena e potersi muovere con più "intelligenza"

è completamente inutile modificare le altre classi del progetto siccome verranno utilizzate quelle di defaut, modificandole si otterranno vantaggi solo nella propria simulazione


