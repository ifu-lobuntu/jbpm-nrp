#1.  Done and tested

ConversationForAction workflow:

- request directly
- promise
- accept
- start
- get initiated commitments
- get owned commitments
- counterToInitiator
- counterToOwner
- renegotiate
- renege
- withdraw

#2. To implement

ConversationForAction workflow:

- add conversation (not direct request)
- counterToRenegotiator
- counterFromRenegotiator
- accept new terms
- allow inputs and outputs to be removed (currently only additive, maybe use null values but keep the keys)
- null dates should not override previous agreed dates
- implement validation for conversationForActionRole (Any, Initiator,Owner,etc.)

Specify ContractService

- prepare contract
- commit to contract
- consummate contract

Introduce custom state transitions for CMMN planning elements:
- Do this in a separate project.
- LifeCycleProvider (associate with human tasks (and stages?)
- pv:nonStandardEvent lifecycleProvider="theId"

Introduce custom state transitions for CaseFileItems:
- Do this in above mentioned project.
- LifeCycle  associate with caseFileItems, list transitions
- pv:nonStandardEvent lifecycleProvider="theId"

Add a 'correlation key' parameter, the central product/offering the initiator is interested in
- used to identify tasks
- could also be used to select the performer of a task, which could be good for decentralized shopping baskets as cases (think Kandu and Tourism)
- In expressions inside a Task, make this CaseParameter available. This would help with automatic selection of other parameters based on one input parameter
- once a user has selected the correlation parameter, have a mechanism to calculate values for the other parameters after planning

Milestone with cost breakdown (move to REA)
- introduce payment service / accounting service with multiple currencies (local, national, etc)
- issue payment automatically when milestone achieved
- a further refinement could even freeze funds on commitment
- issue data to VDML db for tasks whose completion are entry criteria ???? what did I mean here

Calendaring
- schedule tasks due date on Google?
- mark certain tasks as meetings and put them on Google Calendar
- look at OptaPlanner

Allowing SentryInstances and OnPartInstances to be created at runtime
e-mail listener that creates tasks
allow user to associate the task with a case type and start the case
