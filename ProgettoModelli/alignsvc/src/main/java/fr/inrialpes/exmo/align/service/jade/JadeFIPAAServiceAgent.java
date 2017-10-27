/*
 * $Id: JadeFIPAAServiceAgent.java 1699 2012-03-09 23:07:32Z euzenat $
 *
 * Copyright (C) Orange R&D, 2006
 * Copyright (C) INRIA, 2006, 2008-2009, 2011-2012
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


package fr.inrialpes.exmo.align.service.jade;

import fr.inrialpes.exmo.align.service.AServProtocolManager;
import fr.inrialpes.exmo.align.service.jade.messageontology.*;
import fr.inrialpes.exmo.align.service.msg.ErrorMsg;
import fr.inrialpes.exmo.align.service.msg.Message;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

import java.util.Iterator;
import java.util.Properties;

/**
 * SubClass JadeFIPAAServiceAgent
 */
public class JadeFIPAAServiceAgent extends Agent {

    private static final long serialVersionUID = 330;
	public static final String SERVICE_NAME = "Alignment";
	public static final String SERVICE_TYPE = "Alignment-service";

	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	private String myId;
	private String serverId;
	private AServProtocolManager manager;
	private int localId=0;
	private Properties initialParameters;

	//	FIPA ACL stuff

	private ContentManager CTmanager=new ContentManager();
	private SLCodec codec=new SLCodec();
	private Ontology ontology=JADEFIPAAlignmentServerOntology.getInstance();


	protected void setup() {
		// myLogger.log(Logger.INFO, getAID().getName()+" started");
		super.setup();
		codec = new SLCodec();

		// ontology = ContextAgentManagerOntology.getInstance();
		CTmanager = this.getContentManager();

		// System.out.println("agent" + getAID() + " "+ getLocalName()+" is
		// created");

		CTmanager.registerOntology(ontology);
		CTmanager.registerLanguage(codec);

		// Read arguments
		Object[] args = getArguments();
		if (args != null) {
			/**
			 * for (int i = 0; i < args.length; ++i) { myLogger.log(Logger.INFO,
			 * "Arg-"+i+" = "+args[i]); }
			 **/

			manager = (AServProtocolManager) args[0];
			initialParameters = (Properties) args[1];
		}

		myId = "LocalJADEInterface";
		serverId = "dummy";
		localId = 0;

		// Add initial behaviours to manage incoming message
		addBehaviour(new InitialBehaviour(this));// end of CyclicBehaviour

		// Register with the DF
		registerWithDF();
	}// end of Setup
	
	private class InitialBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 330;

		public void action() {

			String perf; // performative
			String info; // parameters
			Properties params = initialParameters;

			MessageTemplate tpl = MessageTemplate.and(
					MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
							MessageTemplate.MatchOntology(ontology.getName())),
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

			ACLMessage msg = myAgent.receive(tpl);
			if (msg != null) {
				// ---------------------------------------------------------
				// myLogger.log(Logger.INFO, "Received message:
				// "+msg.toString());

				try {
					ContentElement ce = null;
					ce = CTmanager.extractContent(msg);
					params = decodeMessage(ce, params);

					handleContentElement(params, msg, ce);
				}

				catch (CodecException ce) {
					ce.printStackTrace();
				} catch (OntologyException oe) {
					oe.printStackTrace();
				}
			} else {
				block();
			}
			params = initialParameters;
		}

		private void handleContentElement(Properties params, ACLMessage msg, ContentElement ce)
				throws CodecException, OntologyException {
			if (ce instanceof ALIGN) {
				instanceOfAlign(params, msg, ce);
			} else if (ce instanceof LOAD) {
				instanceOfLoad(params, msg, ce);
			} else if (ce instanceof RETRIEVE) {
				instanceOfRetrieve(params, msg, ce);
			} else if (ce instanceof STORE) {
				instanceOfStore(params, msg, ce);
			} else if (ce instanceof FIND) {
				instanceOfFind(params, msg, ce);
			} else if (ce instanceof CUT) {
				instanceOfCut(params, msg, ce);
			} else {
				ACLMessage JADEanswer = msg.createReply();
				JADEanswer.setLanguage(codec.getName());
				JADEanswer.setOntology(ontology.getName());
				JADEanswer.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				myAgent.send(JADEanswer);
			}
		}

		private void instanceOfAlign(Properties params, ACLMessage msg, ContentElement ce)
				throws CodecException, OntologyException {
			Message answer = manager.align(new Message(newId(), (Message) null, myId, serverId, "", params));
			if (!(answer instanceof ErrorMsg)) {
				ACLMessage JADEanswer = msg.createReply();
				JADEanswer.setLanguage(codec.getName());
				JADEanswer.setOntology(ontology.getName());
				JADEanswer.setPerformative(ACLMessage.INFORM);
				// JADEanswer.setContent(answer.getContent());
				((Action) ce).setResult(answer.getContent());
				CTmanager.fillContent(JADEanswer, ce);
				myAgent.send(JADEanswer);
			} else {
				myLogger.log(Logger.WARNING, answer.getContent());
			}
		}

		private void instanceOfLoad(Properties params, ACLMessage msg, ContentElement ce)
				throws CodecException, OntologyException {
			Message answer = manager.load(new Message(newId(), (Message) null, myId, serverId, "", params));
			if (!(answer instanceof ErrorMsg)) {
				ACLMessage JADEanswer = msg.createReply();
				JADEanswer.setLanguage(codec.getName());
				JADEanswer.setOntology(ontology.getName());
				JADEanswer.setPerformative(ACLMessage.INFORM);
				((Action) ce).setResult(answer.getContent());
				CTmanager.fillContent(JADEanswer, ce);
				// JADEanswer.setContent(answer.getContent());
				myAgent.send(JADEanswer);
			} else {
				myLogger.log(Logger.WARNING, answer.getContent());
			}
		}

		private void instanceOfRetrieve(Properties params, ACLMessage msg, ContentElement ce)
				throws CodecException, OntologyException {
			Message answer = manager.render(new Message(newId(), (Message) null, myId, serverId, "", params));
			if (!(answer instanceof ErrorMsg)) {
				ACLMessage JADEanswer = msg.createReply();
				JADEanswer.setLanguage(codec.getName());
				JADEanswer.setOntology(ontology.getName());
				JADEanswer.setPerformative(ACLMessage.INFORM);
				// JADEanswer.setContent(answer.getContent());
				((Action) ce).setResult(answer.getContent());
				CTmanager.fillContent(JADEanswer, ce);
				myAgent.send(JADEanswer);
			} else {
				myLogger.log(Logger.WARNING, answer.getContent());
			}
		}

		private void instanceOfStore(Properties params, ACLMessage msg, ContentElement ce)
				throws CodecException, OntologyException {
			Message answer = manager
					.store(new Message(newId(), (Message) null, myId, serverId, params.getProperty("id"), params));
			if (!(answer instanceof ErrorMsg)) {
				ACLMessage JADEanswer = msg.createReply();
				JADEanswer.setLanguage(codec.getName());
				JADEanswer.setOntology(ontology.getName());
				JADEanswer.setPerformative(ACLMessage.INFORM);
				// JADEanswer.setContent(answer.getContent());
				((Action) ce).setResult(answer.getContent());
				CTmanager.fillContent(JADEanswer, ce);
				myAgent.send(JADEanswer);
			} else {
				myLogger.log(Logger.WARNING, answer.getContent());
			}
		}

		private void instanceOfFind(Properties params, ACLMessage msg, ContentElement ce)
				throws CodecException, OntologyException {
			Message answer = manager
					.existingAlignments(new Message(newId(), (Message) null, myId, serverId, "", params));
			if (!(answer instanceof ErrorMsg)) {
				ACLMessage JADEanswer = msg.createReply();
				JADEanswer.setLanguage(codec.getName());
				JADEanswer.setOntology(ontology.getName());
				JADEanswer.setPerformative(ACLMessage.INFORM);
				// JADEanswer.setContent(answer.getContent());
				((Action) ce).setResult(answer.getContent());
				CTmanager.fillContent(JADEanswer, ce);
				myAgent.send(JADEanswer);
			} else {
				myLogger.log(Logger.WARNING, answer.getContent());
			}
		}

		private void instanceOfCut(Properties params, ACLMessage msg, ContentElement ce)
				throws CodecException, OntologyException {
			Message answer = manager.trim(new Message(newId(), (Message) null, myId, serverId, "", params));
			if (!(answer instanceof ErrorMsg)) {
				ACLMessage JADEanswer = msg.createReply();
				JADEanswer.setLanguage(codec.getName());
				JADEanswer.setOntology(ontology.getName());
				JADEanswer.setPerformative(ACLMessage.INFORM);
				// JADEanswer.setContent(answer.getContent());
				((Action) ce).setResult(answer.getContent());
				CTmanager.fillContent(JADEanswer, ce);
				myAgent.send(JADEanswer);
			} else {
				myLogger.log(Logger.WARNING, answer.getContent());
			}
		}

	}

	

	protected void takeDown() {

		myLogger.log(Logger.INFO, "Agent Alignement Service close");
		this.doDelete();
	}

	private void registerWithDF() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName()+'-'+SERVICE_NAME);
		sd.setType(SERVICE_TYPE);
		dfd.addServices(sd);
		try {
			//myLogger.log(Logger.INFO, "Registering with DF...");
			DFService.register(this, dfd);
			//myLogger.log(Logger.INFO, "Registration OK.");
		}
		catch (FIPAException fe) {
			myLogger.log(Logger.WARNING, "Error registering with DF.", fe);
		}
	}

	private int newId(){return localId++;}

    private Properties decodeMessage(ContentElement ce, Properties param){
	Properties toReturn = param;
	Action action= (Action)ce;
	for( Iterator<Parameter> iter = action.getAllHasParameter(); iter.hasNext(); ){
	    Parameter OntoParam = iter.next();
	    toReturn.setProperty( OntoParam.getName(), OntoParam.getValue() ); 
	}
	return toReturn;
    }


}
