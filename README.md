# Quantum
A software for Android that emulates the actions of a quantum computer

> It's main purpose is to enable testing to everyone, even on the go. 
> Since this app is only an emulator, it can show the position of the qubits on the bloch sphere.
> This app can also show the vector representations of the states in a straightforward way.
> The limit is only the computing power of your device!

### About the backend
> The app uses a home-made backend written entirely in Java. Since the app does not use
> Qiskit, no such functionality is included. Scripts cannot be run, however, it is planned!

### Main features

- 10 qubits
- Gates with at most 4 qubits
- Parallel processing (1-64 threads)
- Extremely high number of shots (up to 2^20)
- Numerous predefined gates
- Users can define custom gates
- Get results immediately &rarr; No queues, everything is done locally
- Export results as .csv
- Import / Export gate sequence & Export as OpenQASM
- Show bloch sphere
- Display the final state-vector of the system
- Graph the probability distribution

### Predefined Gates
##### Single Qubit
- Hadamard
- Pauli-X/Y/Z
- S-gate, T-gate (π/2 & π/4 phase shift)
- √NOT
- Identity
- U3
- Rotation (using spherical coordinates)

##### Multi Qubit
- Controlled U3-gate
- CNOT/CY/CZ (Controlled Pauli)
- Controlled T and S gates
- Controlled Hadamard
- SWAP
- Toffoli (CCNOT)
- Fredkin (CSWAP)
- Quantum Fourier Transform (up to 6 qubits)

#### Desktop key shortcuts
|Key|Action
|---------|------|
| Ctrl+Z  |  Undo
| Ctrl+Y  |  Redo
| Ctrl+S  |  Save
| Ctrl+E  |  Export
| Ctrl+D  |  Clear timeline
| Ctrl+R  |  Run
| Ctrl+M  |  Switch statevector mode
| Ctrl+B  |  Show Bloch sphere
| Ctrl+O  |  Open file
| Ctrl+A  |  Add gate
| Ctrl+&rarr; |  Scroll right
| Ctrl+&larr; |  Scroll left
| Ctrl+&uarr; |  Scroll up
| Ctrl+&darr; |  Scroll down
| W |  Go up
| A |  Go left
| S |  Go down
| D |  Go right
| E |  Edit selected
| M |  Toggle qubit measurement on/off

###### Planned features
Display multi-qubit Bloch sphere
Create and run algorithms with loops and conditions  
Display the final unitary matrix of the system

<br/>
<a href='https://play.google.com/store/apps/details?id=hu.hexadecimal.quantum&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'>
<img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="150"/></a>
<br/>