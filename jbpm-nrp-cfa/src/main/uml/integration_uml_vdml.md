#Role of UML, JPA and tables
#1. Role of UML in CMMN
UML is used to define the case folder, as CMMN's case folder model is a bit simplistic.
At some point, we may look at UML to CMMN transformation for the caseFolder, but we can implement CMMN CaseFile semantics without that.
In short, UML is the master here. Whether tables or JPA classes are used here would be dependent on the CaseFile implementation being used.

#2. Categories of VDML Data at runtime
Tables store 4 distinct types of data

##2.1. Organizational Data
This is initial data that sets up organizational relationships and ownership/custodianship. This data is also used to do optimal
role assignments in CMMN. This data is mostly populated when a VDML model is deployed, and/or when an economic agent registers and fulfills certain roles and OrgUnitTemplates. As described in the section about OrgUnitTemplates,some of the data is pre-structured, other 
data will be configured for the needs of the specific economic agent. For the former, tables will be generated, for the latter, generic tables will be used(e.g.store,org_unit,etc.) 

OrgUnits/OrgUnitTemplates
Stores
Pools
Roles
Assignments

##2.2 Accounting and Resource Planning Data
This data is extracted from the forms that allow participants to act on individual tasks. The significance of this category of data
is that it is used to increase/decrease inventory levels of Stores, which, in the case of Money stores, means we're doing accounting.
All of this data gets logged, and the increase/decrease of the Stores gets applied, within the context of a CapabilityMethod instance.

###2.2.1. ResourceUses and Deliverable Flows 
A ResourceUse tells us the quantity of a specific Inputport's resource that is being used, and the DeliverableFlow tells us which Store it should come from. This store's inventory will therefore be decreased. ResourceUse allows us to establish the duality that REA needs us to track. Only ResourceUses linked to tangible DeliverableFlows would typically be considered for accounting purposes. Between ResourceUses and DeliverableFlows, we may in future want to think of some shorthand for a money exchange.

###2.2.2. ValueAdd 
ValueAdds contribute to a pre-committed ValueProposition is used to indicate the contributions of a Participant to the total
of related Value of the given CapabilityInstance. The significance of these contributions would usually be made explicit by an 
outputPort with ValueAdds, and this would allow us to do "ValueContributionEquations" as Bob's system does. This particular use
of ValueAdds is not a high priority just yet.

##2.3. Performance Data
This data overlaps with the Accounting and Resource Planning data, but here the interest is more in long term trends, and also in intangible value exchanges. This data is primarily for reputation/performance calculation purposes. These tables will more or less follow a star schema to allow for easy aggregation over multiple dimensions. Dimensions will be determined from two sources: 
 - metadata (e.g. VDML model elements such as Activities, Stores)
 - organizational data, discussed above.
Such a star schema would allow an OLAP tool such as Saiku on Mondrian to give the user access to highly customizable queries. Queries can potentially also be visualized in jBPM Dashbuilder. It would not make sense to have JPA entities for this data, as the data would be additive (insert only)  and JPA would just add overheads. 
Every ActivityInstance will log two records, extracted from the form data associated with the activity:

###2.3.1. A record representing ValueAdd contributions
This record will be extracted from the ValueAdds on OutputPorts for which the two performing roles involved in the DeliverableFlow have ValuePropositions. The question as to which of the two performers gets the final say in these measurements is up to the developer of the forms involved, and there may also be cost vs satisfaction level distinction here.
 
###2.3.2. A record representing its CapabilityMeasurements. 
This record will be extracted from the MeasuredCharacteristics of the Activity that can be mapped to the MeasuredCharacteristics of the the Capability using its trait.

##2.4. Data Aggregated from Performance Data 
There are three places where data aggregations will be calculated and stored continuously whether there is a dashboard or cube requirement for it or not: CapabilityOffers, Stores and ValuePropositions. The reason for this is that this data will be accessed continously for users to determine whose services they want.  

###2.4.1. CapabilityOffer  
aggregated from ActivityInstances, using the underlying Capability to determine which measures to aggregate

###2.4.2. ValueProposition and ValuePropositionComponents
Aggregated from ValueAdds extracted from ActivityInstances

###2.4.2. Stores
Aggregated from InputPorts extracted from downstream ActivityInstances that received input from the stores. The focus here is on the quality of the BusinessItem received and their MeasuredCharacteristics. In addition to this historic data, Stores of non-fungible resources in addition contain links to individual instances, but the store itself will also contain aggregates of the MeasuredCharacteristics of instances. Stores of fungible resources would contain only information pertaining to its entire content (totals, averages etc.) maintained manually as a single record.

#3. Role of UML in Organizational modeling
It is important to keep in mind that, because of our proposed business model, we deviated from / extend VDML by interpreting OrgUnits in two ways.

##3.1. When configuring a ValueNetwork, we interpret OrgUnits as templates for OrgUnits. 
In this context, the OrgUnit is seen as a template with predefined relationships to RoleDefinitions, Capabilities and "StoreDefitions" (not in VDML). When a business owner registers a business, Stores, Roles and CapabilityOffers will automatically
be created from the OrgUnit template(s) the business owner selected. Individual JPA classes and tables will be generated for these, as 
there will be many instances of them.  

##3.2. When configuring an individual business, we interpret OrgUnits as standard VDML OrgUnits
The business owner can still configure unique Stores, CapabilityOffers and Positions for the OrgUnit. At this level, some level
of standardization is still possible using RoleDefinitions and Capabilities, and individual JPA classes and tables will be generated
for such standard entities.

The two interpretations are complementary, but the latter is lower priority, as it will only be required once businesses have a need 
to distinguish itself. At runtime, we still use standard VDML semantics to interpret OrgUnits, and an OrgUnit is still interpreted as an instance. 

UML can be used to refine Organizational data in VDML. In this context, VDML is the master. When we import a VDML element into a Class
diagram, we retain that link to the VDML element. Care should be taken not to duplicate CollaborationStructure (Roles, Assignments) or CapabilityManagement (Stores, Capabilityoffers) relationships in UML. This is low priority as we have very few data requirements that are not met by VDML. 

#4. Role of UML in Library modeling
At this point, we are primarily interested in BusinessItemdefinitions. We intend to create a simple UML DiagramProfile for VDML extensions. In the UML profile, use stereotypes to link to the actual VDML Elements, e.g. BusinessItemDefinition, ValueDefinition, RoleDefinition and Capability. We will add a MeasuredCharacteristic Compartment that writes directly to the SMM file.
In the case of BusinessItemLibraryElements, BusinessItemCategory's are abstract supertypes of BusinessItemDefinitions in UML. In the case of Capabilities, this UML-oriented modeling technique would be a temporary alternative until we do proper Capability modeling. In future, we may want to extend the UML modeling further to allow for intelligent SMM relationships amongst related MeasuredCharacteristcs

#5. UML, VDML and CMMN.
The idea is that anything that can be represented in UML could be a CaseFileItem in CMMN. By implication, this means we will make VDML org structures and BusinessItemDefinitions available in CMMN. OrgStructures will generally have a life outside of the case instance, and will therefore be presented as "linked" items. However, many BusinessItems (e.g. order line, invoice, etc.) would live only inside the case instance. 

 