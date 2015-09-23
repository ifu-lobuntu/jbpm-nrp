# 1. Overview

This project provides an implementation of Fernando Flores' ["Conversation for Action"](http://coevolving.com/blogs/index.php/archive/conversations-for-action-commitment-management-protocol/) (CFA) protocol that would allow participants to 
negotiate the conditions of their collaboration, and the state of completion of their work. 

# 2. CFA Protocol

![CFA Protocol](http://coevolving.com/blogs/wp-content/uploads/2008/05/1986_winograd_flores_figure_51.jpg)

The CFA Protocol can be summarized as follows:

- The initiator of a Task asks a potential owner of a Task if he/she wants to perform the task, and that the planned state of the Task (Inputs, Deadlines, expected Outputs) is acceptable.

- The potential owner of a task to accepts, rejects or makes a counter proposal on the the state of the task.

- The initiator of a Task to accepts, rejects our counters the counter proposal.

- If not rejected, both parties eventually accept the planned state of the task, and now it is considered a "contractual" commitment.

- On completion, the actual owner of the task declares it is completed adequately.

- The initiator accepts it, or declares that is is still inadequate.

- Either party can reject/withdraw, which implies a breach of contract.

- If rework was required, the actual owner can again declare it is completed adequately.

- If the initiator is satisfied, the contract is deemed fulfilled.

# 3. Applications for the CFA Protocol

The CFA Protocol allows participants much more freedom when it comes to task selection. It may not be ideal for military-style command-control organizations the expect employees to follow orders no matter what. However, for situations where the participants in a process are considered peers, this workflow provides a solid basis for individuals to amicably negotiate the work to be done, and agree on the completion state. As such, this protocol is ideal for scenarios where there is a collaboration between economically independent entities, such as B2B, and also between customers and freelance workers. The fact that the protocol explicitly highlights the point where a commitment is made, and where the commitment is fulfilled, also allows for the contractual details and accounting to be automated during the process. 

# 4. More Extensions to the Planning activities

- Creating Sentries, OnParts and Conditions 'on the fly'

- Retrieving performance information regarding potential owners of a task to select the appropriate actual owner

- Using Opta Planner to schedule planned tasks. Some applications for this would be:

  - Finding ideal bookings periods for holiday resorts.

  - Finding ideal time of appointments e.g. Doctors, Physios, Hairdressers, Beauty Therapists, etc.
 
  - Allowing participants to plan and assess their capacity in advance.

- Integration with Google Calendar and Google Tasks
