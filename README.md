# Quantum
A software for Android that emulates the actions of a quantum computer

> It's main purpose is to enable testing to everyone, even on the go. 
> Since this app is only an emulator, it can show the position of the qubits on the bloch sphere.
> This app will also show the matrix and vector representations of the states and operators in a straightforward way.
> The limit is only the computing power of your device!

### About the backend
> The app uses a home-made backend written entirely in Java. Since the app does not use
> Qiskit, no such functionality is included. Scripts cannot be run, however, it is coming soon!

### Main features

- At most 10 qubits
- Gates with at most 4 qubits
- Parallel processing (1-64 threads)
- Extremely high number of shots (up to 2^20)
- Lots of predefined gates
- Get results immediately
- Export results
- Import / Export gate sequence
- Show bloch sphere
- Display the final state-vector of the system

### Predefined Gates
##### Single Qubit
- Hadamard
- Pauli-X/Y/Z
- S-gate (π/2 phase shift)
- T-gate (π/4 phase shift)
- √NOT
- Identity
- U3
- Rotation (using spherical coordinates)

##### Multi Qubit
- CNOT/CY/CZ (Controlled Pauli)
- Controlled T-gate
- Controlled S-gate
- Controlled Hadamard
- SWAP
- Toffoli
- Fredkin

###### Planned features
Create and run algorithms with loops and conditions  
Display the final unitary matrix of the system

<br/>
<a href='https://play.google.com/store/apps/details?id=hu.hexadecimal.quantum&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'>
<img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="150"/></a>
<br/>
<a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">
<img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" /></a>
<br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License</a>.
