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

- At most 8 qubits
- Gates with at most 4 qubits
- Parallel processing (1-64 threads)
- Extremely high number of shots (up to 2^20)
- Lots of predefined gates
- Get results immediately
- Export results
- Import / Export gate sequence
- Show bloch sphere

### Predefined Gates
##### Single Qubit
- Hadamard
- Pauli-X/Y/Z
- S-gate (π/2 phase shift)
- T-gate (π/4 phase shift)
- π/6 phase shift
- √NOT
- Identity

##### Multi Qubit
- CNOT/CY/CZ (Controlled Pauli)
- Controlled T-gate
- Controlled S-gate
- Controlled Hadamard
- SWAP
- Toffoli
- Fredkin
- 2 & 3 Qubit Identity

###### Planned features
Create and run algorithms with loops and conditions  
Apply rotation gates with custom angles more easily  
Display the final state-vector of the system

(C) All rights reserved! (for now)
