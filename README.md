# μEnergia

### μEnergia: C++ Framework to Develop Embedded Software 

**μEnergia**, pronounced as "micro-Energia", is a software development platform for **TivaC Series EK-TM4C123GXL LaunchPad** and **Tiva C Series TM4C129 ConnectedLaunchPad**. The framework is lightweight, flexible, and consumes minimum memory and computational resources to build applications and rational agents on microcontrollers that sense and actuate using add-on boards.

μEnergia consists of two parts:

1. **lm4f** : The core functionality is provided by extending Energia lm4f module. 
2. **muenergia**: This code provides configuration of all the modules and representations that we used in the experiments. 

To install μEnergia, follow these instructions:

1. **lm4f**:
   1. Download Energia 13 from [http://energia.nu/download/](http://energia.nu/download/).
   2. cd to the **energia-0101E0013/hardware/lm4f/cores/lm4f**.
   3. Delete the **main.cpp**, **startup_gcc.c**, **WString.h**, and **WString.cpp** files. 
   4. git clone [https://github.com/samindaa/muenergia.git](https://github.com/samindaa/uEnergia.git)
   5. Copy all the content inside **lm4f** directory to **energia-0101E0013/hardware/lm4f/cores/lm4f**.
   
2. **muenergia**:
   1. Attach the sensor hub to BoosterPack 1, if you are using a Tiva C Series TM4C1294 Connected LaunchPad Evaluation Kit.
   2. Open Energia GUI and select **muenergia.ino** within the **muenergia** directory.
   3. Select the appropriate board configuration and upload the program.  

μEnergia framework consists:

1. Template.h
2. Framework.h
3. Config.h
4. Framework.cpp
5. FrameworkDebug.cpp
6. Config.cpp

We have also used the framework with: **MSP-EXP430G2 LaunchPad**. 

The **lm4f/framework** directory contains all the framework files. Our framework is
independent of hardware specific configurations. Therefore, one can use the 
framework for other applications (e.g., [topological sorting](http://en.wikipedia.org/wiki/Topological_sorting)). Use the provided **configure** script to build a native binary for such applications. 

**datasets** contain the data we collected from humans and robots in our experiments. 

###### User Guide

This is an open source project that provides a software development and a machine learning environment using:

1. [Tiva C Series TM4C123G LaunchPad microcontroller](http://www.ti.com/tool/ek-tm4c123gxl), is a low-cost evaluation platform for ARM Cortex-M4F-based microcontrollers from Texas Instruments;

2. [Tiva C Series TM4C129 Connected LaunchPad](http://embeddedcomputing.weebly.com/connected-launchpad-tiva-c-series-tm4c129.html),  is a new development platform from Texas Instruments based on the powerful TM4C129;

3. [Sensor Hub BoosterPack](http://www.ti.com/tool/boostxl-senshub), is an add-on board designed to fit the Tiva C Series TM4C123G LaunchPad along with all of TI’s MCU LaunchPads; 

4. [Energia](http://energia.nu/), is an open-source electronics prototyping platform; and

5. [RLLib](http://web.cs.miami.edu/home/saminda/rllib.html), is a C++ Template Library to Predict, Control, Learn Behaviors, and Represent Learnable Knowledge using On/Off Policy Reinforcement Learning, and Supervised Learning. 


###### Usage 

The project consists of four parts. It consists of modules and representations that runs on:

1. Microcontrollers (incremental);

2. Offline (batch mode); 

3. [NAO](http://www.aldebaran.com/) robots; and

4. Visualizations. 

###### Software Architecture

The software development framework uses a notion of `modules` and `representations` to perform computations. The modules implement functions, while representations exchange information from one module to another. The following figure shows an example of modules and representations currently available in the distribution. 

![](http://web.cs.miami.edu/home/saminda/tmp/csc688_energia.png)

The green boxes represent modules, while the yellow ellipses represent the representations. As an example, the module `MPU9150Module` contains logic to read/write  from MPU-9150 Nine-Axis (Gyro + Accelerometer + Compass) MEMS MotionTracking™ device on the sensor hub booster pack. The representation `MPU9150Representation` contains all the values that module MPU9150Module would like to share with other modules. In this graph TestModule4 requests values from MPU9150Representation to implement its logic. A module can provide multiple representations as shown in the module `TestModule2`. The yellow arrows shows the provided representations, the black arrows show the requested representations, and the red arrows show the used representations.  

There are two graphs in the figure; the "offline graph" will be executed offline, while the rest of the graph, "online graph", will be executed on the devices. We have shown only two graphs in this figure; in reality, one can keep up to N number of graphs. The uses of the software only requires to write the modules and representations, while the framework will compute the topologically sorted graph out of the nodes. This will be computed once online/offline, and the nodes in the queue will be executed one after the other. If there were to be cycles in the graph, the framework will detect them and indicate them to the users.  

The module `PredictionModule` uses RLLib to learn on-line about a supervised learning problem. It provides the representation `PredictionRepresentation`. We have written the module `SendModule` to send values of the representations to offline modules. This is useful during the debug phases of the project. The module `RS232Module` reads the data values that is send from SendModule. 

To write modules and representations, one needs the following (this example shows the implementation of the module  TestModule1 and the representation TestRepresentation1 in the figure):


```cpp

#pragma once

#include "Template.h"
#include "LaunchPadRepresentation.h"
#include "TestRepresentation1.h"

MODULE(TestModule1)
  REQUIRES(LaunchPadRepresentation) //
  PROVIDES(TestRepresentation1) //
END_MODULE

class TestModule1: public TestModule1Base
{
  public:
    void update(TestRepresentation1& theTestRepresentation1);
};

```

```cpp

#include "TestModule1.h"

void TestModule1::update(TestRepresentation1& theTestRepresentation1)
{
  theTestRepresentation1.leftButton = theTestRepresentation1.rightButton = false;
#if defined(ENERGIA)
  if (digitalRead(PUSH1) == LOW)
    theTestRepresentation1.leftButton = true;
  if (digitalRead(PUSH2) == LOW)
    theTestRepresentation1.rightButton = true;
#endif
}

MAKE_MODULE(TestModule1)


```

```cpp

#pragma once

#include "Template.h"

REPRESENTATION(TestRepresentation1)
class TestRepresentation1: public TestRepresentation1Base
{
public:
  bool rightButton, leftButton;
  TestRepresentation1() :
    rightButton(false), leftButton(false)
  {
  }
};


```

The user needs to include the header file `Template.h`  to access the framework functionality. 



###### RLLib

Following figure shows an example of using RLLib functionality in a module.

```cpp

/*
 * PredictionModule.h
 *
 *  Created on: Mar 20, 2014
 *      Author: sam
 */

#ifndef PREDICTIONMODULE_H_
#define PREDICTIONMODULE_H_

#include "Template.h"
//#include "ISL29023Representation.h"
#include "PredictionRepresentation.h"
#include "SupervisedAlgorithm.h"
#include "ControlAlgorithm.h"
#include "RL.h"
#include "Projector.h"

MODULE(PredictionModule)
  PROVIDES(PredictionRepresentation) //
END_MODULE
class PredictionModule: public PredictionModuleBase
{
  private:
    // Supervised Learning
    int nbTrainingSample;
    int nbMaxTrainingSamples;
    float gridResolution;
    int nbTilings;
    //
    RLLib::Random<float>* random;
    RLLib::Vector<float>* x;
    RLLib::SemiLinearIDBD<float>* predictor;

    // Reinforcement Learning
    float epsilon;
    RLLib::RLProblem<float>* problem;
    RLLib::Hashing<float>* hashing;
    RLLib::Projector<float>* projector;
    RLLib::StateToStateAction<float>* toStateAction;
    RLLib::Trace<float>* e;
    float alpha;
    float gamma;
    float lambda;
    RLLib::Sarsa<float>* sarsa;
    RLLib::Policy<float>* acting;
    RLLib::OnPolicyControlLearner<float>* control;

    RLLib::RLAgent<float>* agent;
    RLLib::Simulator<float>* sim;

  public:
    PredictionModule();
    ~PredictionModule();
    void init();
    void execute();
    void update(PredictionRepresentation& thePredictionRepresentation);
};

#endif /* PREDICTIONMODULE_H_ */


```

```cpp

/*
 * PredictionModule.cpp
 *
 *  Created on: Mar 20, 2014
 *      Author: sam
 */

#include "PredictionModule.h"

MAKE_MODULE(PredictionModule)

PredictionModule::PredictionModule() :
    nbTrainingSample(0), nbMaxTrainingSamples(0), gridResolution(0), nbTilings(0), random(0), x(0), predictor(
        0), epsilon(0), problem(0), hashing(0), projector(0), toStateAction(0), e(0), alpha(0), gamma(
        0), lambda(0), sarsa(0), acting(0), control(0), agent(0), sim(0)
{

}

PredictionModule::~PredictionModule()
{
  if (random)
    delete random;
  if (x)
    delete x;
  if (predictor)
    delete predictor;
  if (problem)
    delete problem;
  if (hashing)
    delete hashing;
  if (projector)
    delete projector;
  if (toStateAction)
    delete toStateAction;
  if (e)
    delete e;
  if (sarsa)
    delete sarsa;
  if (acting)
    delete acting;
  if (control)
    delete control;
  if (agent)
    delete agent;
  if (sim)
    delete sim;
}

void PredictionModule::init()
{
  //
  nbTrainingSample = 0;
  nbMaxTrainingSamples = 1000;
  gridResolution = 10;
  nbTilings = 4;
  //
  random = new RLLib::Random<float>;
  hashing = new RLLib::MurmurHashing<float>(random, 32);
  problem = new RLLib::RLProblem<float>(random, 1, 1, 1); // Dummy
  projector = new RLLib::TileCoderHashing<float>(hashing, problem->dimension(), gridResolution, 4,
      true);
  //
  x = new RLLib::PVector<float>(problem->dimension());
  predictor = new RLLib::SemiLinearIDBD<float>(projector->dimension(), 0.01f);
  //
  toStateAction = new RLLib::StateActionTilings<float>(projector, problem->getDiscreteActions());
  e = new RLLib::RTrace<float>(projector->dimension());
  //
  alpha = 0.001f / projector->vectorNorm();
  gamma = 0.10f;
  lambda = 0.3f;
  epsilon = 0.01f;
  //
  sarsa = new RLLib::Sarsa<float>(alpha, gamma, lambda, e);
  acting = new RLLib::EpsilonGreedy<float>(random, problem->getDiscreteActions(), sarsa, epsilon);
  //
  control = new RLLib::SarsaControl<float>(acting, toStateAction, sarsa);
  agent = new RLLib::LearnerAgent<float>(control);
  sim = new RLLib::Simulator<float>(agent, problem, 1000, 1, 1);

}

void PredictionModule::execute()
{
  if (nbTrainingSample < nbMaxTrainingSamples)
  {
    float rnd = random->nextReal() - 1.0f;
    x->setEntry(0, (rnd + 1.0f) / 2.0f);
    predictor->learn(projector->project(x), 0.0f);

    rnd = random->nextReal();
    x->setEntry(0, (rnd + 1.0f) / 2.0f);
    predictor->learn(projector->project(x), 1.0f);

    ++nbTrainingSample;
  }
}

void PredictionModule::update(PredictionRepresentation& thePredictionRepresentation)
{
  if (random->nextReal() > 0.5f)
  {
    float rnd = random->nextReal() - 1.0f;
    x->setEntry(0, (rnd + 1.0f) / 2.0f);
    thePredictionRepresentation.target = 0.0f;
    thePredictionRepresentation.prediction = predictor->predict(projector->project(x));
  }
  else
  {
    float rnd = random->nextReal();
    x->setEntry(0, (rnd + 1.0f) / 2.0f);
    thePredictionRepresentation.target = 1.0f;
    thePredictionRepresentation.prediction = predictor->predict(projector->project(x));
  }
}




```


```cpp

/*
 * PredictionRepresentation.h
 *
 *  Created on: Apr 2, 2014
 *      Author: sam
 */

#ifndef PREDICTIONREPRESENTATION_H_
#define PREDICTIONREPRESENTATION_H_

#include "Template.h"

REPRESENTATION(PredictionRepresentation)
class PredictionRepresentation: public PredictionRepresentationBase
{
  public:
    float target;
    float prediction;
    PredictionRepresentation() :
        target(0), prediction(0)
    {
    }
};

#endif /* PREDICTIONREPRESENTATION_H_ */


```


###### Contact

Saminda Abeyruwan (saminda@cs.miami.edu)

###### License

Apache License, Version 2.0

