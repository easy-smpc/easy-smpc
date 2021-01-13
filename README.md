# Secure Histograms via Email

This project aims to provide a simple, easy to use application for calculating
histograms of distributed data using email a a communication channel.

As email provides no safeguard for a faithful protocol execution, we aim to
provide GUI safeguards to help executing the protocol.

At the moment two secret sharing techniques are implemented:
 1. Arithmetic Secret Sharing on the Field (2^127-1) (being the 12th mersenne
    prime) and providing security for counts up to 2^127-2,
 2. Shamir's Secret Sharing with the prime 2^19-1 (being the 6th mersenne prime)
    and providing security for counts up to 2^19-2.
    
This project is partly financed by the "Collaboration on Rare Diseases" of the
Medical Informatics Initiative, funded by the German Federal Ministry of
Education and Research (BMBF).
