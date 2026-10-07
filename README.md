# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```


## Phase 2: Chess Server Design

[My Chess Server Sequence Diagram]([(https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=FAQwxgLg9gTgBAYQDYEsCmA7CwAOIYQpgp5ZwDKaMAblbvocaRHABIgYAmSdeBRJDi0o0iaevyZC4AERAQQAQTBg0AZzXBO8kACMQatHE67gwTBBgBPNXmIYA5nAAMAOgCcZhzCgBXHADEACwAzAAcAEzuYHABqA4AFhC6SL5GAEpoDihqlvIoUBjAyOhkALQAfBRUtDAAXHAA2gAKAPLkACoAunAA9L6GMAA6GADeAEQDVBggALZo43XjcOMANCt4GgDusJyLy2sraLMgKEj7KwC+wCK1cJVsHNxUDRNTMDPzF4fjm2o7MD2SxW63Gx1O52B42u7C4PHgD1uYgaMCyOQgVAAFJlsrkqJkAI5pXIASmAulRIAA1nAALI5NQoRxwVFElCozhwABm6CQnMx70+aHWfwBnHWsDg4LOZKRqnuD1hz3qcAAQiBOITiRAAKIAD1UOEIhWASvhCqqtxeao1WvUuoNaCNBSKVoRDxKFgaQWczhGE3mGhADgWDXGOpgPhV+k5rO10PMXBuNTE9yqcgUylUGgaIYgAFVBgLBkKyRmlCp1Go08ZdA0AGJMzmFqjluC6KxwQVzcQUtDU2Q6fSGbkQ3yo4DlrNVi3VUSqBpTysafWG42ulPysqKp7wxc6acrx3Ok1mqizt37zPLtSrp3r5M0c-bqqerANACsvv940DamDoYrBGUYNNoCjDkYXJjqiCaYJw5KUjSLbwFsKAQAkXYlj2cAgEglKcJ2aB6uimhLtm1YvnO1DInAyHlo+1FbjucLWooeH9gRHTUpgd4nkUZ7upaNSsexGpWFxVI8ceD5urOb4QN6zghD+f4AYsQGRrADS4fhnYKJJGCwUmZEzpRcpoA0GC+EgSAMamDwmTmcBgJSGLIcWrY6GWB43jWJgNIonDNoM9HmTWjlqA0Ln9hiii+OhmIgPFCTlt517kX5dZwIFnJxehoWbkYlECQ0OLovi6jWdgAkXsJKpvFhXxwGGIIrEl6EdFABnLC1CayZR8kNBE35jJMjULM1KyteM7UJJ13WTQc1xwV4Pj+AEsAcCGsT1ggOoyAgihwAAMlA2RFPJNaXk0bSdD0vSGBoLo-t2XxQqCoq7N81z9VUJVwA10w9t8H0GP8X1QjCu7PkJ84WXASBnUymKneddqkgh-Y0vST3MnG7JoJyPJoHydlMX90MquqmpoES9q8Q+NVmXVDTU+jDpri6DHPh6qBenAPp+qNqkhup4aadGGosrT8bLUmYUOT55G5mgBZFq9aBpRWGUPP5cCNlwtEhTo7adhrmMDuWEGjmc47iBFtVw1e2tVgzXMK8xyrO4et7SVzTOIizg7pa7fsmv1vOlApcBfkLAZVmpYbAVpxhDgYkHQQscvwQ7zNO0bnkKHAkpWTZFtIYMcAYFALBcn4huSp9gLGFA6hVzXcAnBAYAJGTRWe3utEzMlsAoAAXoTbunpTju1A0+bD+ho8T5wU8bk+gmIHz74C84ACMKkJ6LScSw0viLwky+E0Z8EK+mStVlFrloHlCSJclqWTg-GiZQFQXZR-HQfdZz-VRkyTIagqqmhnnnOeAMxpAzegcUEs15qYG+tzTeg04DDTjggj4wN3ptWSmgwykNEzwWAN4PwgRvBoEwLEeISQ6EMNRn4bAl1A4bwaI0GQOpjo6g6Dqe6j1GSFBGLNZe+RCjaRIV1TAmCaz-VQfI-iMDYaMXhojBw7CUZnXYezWUhVwrf0inAPMr934dVUVrH2v99ZNgAflE2HYcJyIMuXYOeh0421SBOXOXDNHexvGvYBxVKbBPIqEgOGjrQRVCRHV829o6x0PkGY+GkQKp3Aj4qCtsYLZ08Sogy7da71xzqY2eNFS62Q9hTFiKoF6SJgOPSeYc1ENNntaJpI8WkrwSXVOSyTFIH2FkfQC4ssnn2aa0vYhSAkaJoq-AqcMTEhycpwEmqsX7JSsXNGxX91kUSqHrGQWyMROJSkAupjwGkNDYfFCBUCYlUWtKMH6gyBrDJwSNd5FDVo0ICByWIOAmQ0mOuiOAABxHsmhOGxJVI0KFgj7oOB7BI3p49pEYFkdYjxv1blezcXihRTNFkLgRuiGFgYUZUthYYsJ98jkqwgJY4pmBbG+V1llA2uVAFF1ceyoofZLZpxHHkvx9tKmwJovE9pYSB5xNMdEmBgSlVHIGRvIZUdPy-N-OMsWycVRgW8eKzON8inuIYdXMp58KlHKqRSmpCr6lEp6UvPpbTObT06bA7pF8r6r3lYkreOrd6jPjukiZRqz4Bs9XM-5CyqJLP5SARlXifYNFQLkal6hMScp1icnljjcJIGhT2eiSbzINAQFAGyaBIAumLlyct8wVmaJAREk6dLAxPKQNVVVCLXjjDRYGdSjQJijrQAASRkOpPeEQQhBFBFsBIaE0DISFCDFYKRwBUk3YQ5BKwp0ADlD1XC6B8rVXyw24J-FOtQ47J09lnfOxdy6VirvXQepB01d1gH3eNbdI6exnt-dCS9-yqFrUCJwAA7O4ZwaBnCxB1CEBAe0ABsiBn6tvEPC15iLbrdD6FOjFHqsXrlxfsgy97QPnr6p811g8hXrGffMMDoYlrQN9eS+G0V5BoFzZiBAz9c0MpFdjBkTInD42BcTPkcBMSnp7EY1Z4S7k2hpnTXIKrfVqqpraGW9Ng2fMjvzQWaT-wZMmSnGM0sdMQAtXfDNN4WVsqtRgAtpki0NkccslxnYhWeKtrkzOhyXY-xlRSuV3r14do00S2L95-aDsI5E0OcXFE3v5qksZUbDWn2yaajO+Ss7-Mk8Smj1qO51ztRFux0X4bOpuf9d1l9416eVF0xpcbZmaruDlnePoI36oKyfKZfWV4WqrYVAKqb00RSfjFITPZlMVq8g1rlvnsr-1zZW6VgSaJTtnS6wlg9RMrfE5VftPHut+vqiB+Yr6GgLqXVewb5md53tGidudr332FOg4CqwJNEZbEYSgRIEBQc2SgBDgAUlAJkeHYj-qpMUZJV0g4tHzCR3oZGpvYuo6Qn8OAkAgFBzAWtiMYD7AAOqsGnUI3oqpjqKAQAAaR+H9t973svMetKx+B5PKdUBp7ABnTOWds459z0EvOAf87JcmilAArZHGBhNI6ZNdxzZJKs40ZMyBTnJJRMmoLhFAnJRdU4lzAM7-02bGd0-Kl511neOYGzzJJYbLP5es9Gor9m4z2mc8YxWzLzGqw8ySrzW3C21j84bALAqgueZC2K0rkqE+mSOzF5Vpn1OKpVMlvid3zQPYy0eLLIbsF5cjYHwrWSTXWwlXbC1lWhWlO5OU3PUX8-NesrUiPJf55Ta9Sln192DPj5mf0ovn3fcWf3lZxOmSU7TMxdN+Zh2+PzecQoRbpiWXCb+95qLO3eV4bbK4v7nip0tzbjaqUxFcj94ooPyyw-Hdds99qLrSvWfLTdmb3LBb5f3RvdfWzSWWMF3JzXfB1JrBofbIBSrUPPETkMAOtSUHSDiPSbieLeyMfbKUSTiQgwAn3dLUg3SCSKSWvMzZfYbJSNfGzGNHCMggg7qQpFzJbLsHAMCVbeYdbNtE2VCdCKuNACHW3KgC-Y5JPWiAQwTG-a5UfQXFUHXLXHsPtAdfTIdAGD7Kg7BH7P5FaYHdabwSnSHaHSwzsZAfseAEAHAcnIgbFTHKObHbhJoPhARIRe6EwAXc7F4CvGGVXfjHgfAfNY-KPMACImAKI3OHbM5HgC5UtTCKgNQdYB9dYIVTQSrULM1MrD-R1eGMvGSNQoI0vQvLLd3IOMo92Rg0NXLPVEWIPFvLPXxDvRAyLT-ffX-O5EIzea6P5Ovb5EwwpIAA))

