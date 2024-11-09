package control;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

public class PillScheduler {

	public enum SchedState {
		INIT, WAIT, ACTIVE, COMPLETED;
	}
	
	private SchedState schedState=SchedState.INIT;

	long startDateMs;		// start time for the current schedule
	
	/*
	 * two variables to track things
	 */
	long currentIteration;	// whats out current iteration 
	long dispenseAtMs;		// current dispence time: [dispenseAtMs-wakeBeforeMs , dispenseAtMs+waitAfterMs]
	
	
	/*
	 * parsed parameters
	 */
	long intervalMs;
	long wakeBeforeMs;
	long waitAfterMs;
	long numOfTreatments;
	
	public PillScheduler(Date start, int num, long every, long wakeBefore, long waitAfter) throws Exception {
		startDateMs=start.getTime();
		intervalMs=every;
		wakeBeforeMs=wakeBefore;
		waitAfterMs=waitAfter;		
		numOfTreatments = num;
	}

	
	private void readyForDispense() {
		System.out.println("Hello, its time to take yours next doze of meds, press the button");
		
	}
	public synchronized void userPressedButton() {
		if (schedState==SchedState.ACTIVE) {
			System.out.println("User got meds!");
			userPressedButton=true;
			notify();
		}
	}
	
	boolean userPressedButton=false;
	
	public static class Escalation {
		long ms;
		String type;
		String message;
		Escalation(long ms, String type, String message) {
			this.ms=ms;
			this.type=type;
			this.message=message;
		}
		public String toString() {
			return new Date(ms).toString()+" "+type+" "+message;
		}
	}
	TreeMap<Long, Escalation> msToEscalation=new TreeMap<>();
	private void generateEscalations() {
		msToEscalation.clear();
		long startOfNext=dispenseAtMs-wakeBeforeMs;
		msToEscalation.put(startOfNext, new Escalation(startOfNext, "START", "Its a begining of the new dispensing cycle."));
		long midOfNext=dispenseAtMs-wakeBeforeMs/2;
		msToEscalation.put(midOfNext, new Escalation(midOfNext, "REMINER", "Dont forget to take your meds!"));
		msToEscalation.put(dispenseAtMs,new Escalation(dispenseAtMs, "DUETIME", "Its due time to press the button."));
		long pastDue=dispenseAtMs + waitAfterMs/2;
		msToEscalation.put(pastDue,new Escalation(pastDue, "PASTDUE", "Please take the meds, you are past due"));
		long end=dispenseAtMs + waitAfterMs;
		msToEscalation.put(end,new Escalation(end, "MISSED", "You have missed you meds and will be reported"));
	}
	private void cleanEscalations(long nowMs) {
		for (Iterator<Entry<Long, Escalation>> it = msToEscalation.entrySet().iterator(); it.hasNext();) {
			Entry<Long, Escalation> entry = it.next();
			long atMs=entry.getKey();
			if (atMs<nowMs) it.remove();
		}	
	}
	
	private boolean handleInitState(long nowMs) {
		// lets figure out our current iteration
		if (nowMs>startDateMs) {
			// we are in the middle
			long numCompletedIterations=(nowMs - startDateMs) / intervalMs;
			
			if (numCompletedIterations > numOfTreatments) {
				schedState=SchedState.COMPLETED;
				return false;
			}
			long nextTimeMs= startDateMs+(numCompletedIterations+1)*intervalMs;
			if (nowMs > nextTimeMs-wakeBeforeMs) {
				// we are in active state for the next dispense
				schedState=SchedState.ACTIVE;
				currentIteration=numCompletedIterations+1;
			} else if (nowMs < startDateMs + numCompletedIterations* intervalMs+waitAfterMs) {
				// we are at the tail end of the previous dispense 
				schedState=SchedState.ACTIVE;
				currentIteration=numCompletedIterations;
			} else {
				// we are in the middle
				schedState=SchedState.WAIT;
				currentIteration=numCompletedIterations+1;
			}

		} else {
			schedState=SchedState.WAIT;
			currentIteration=0;
		}
		dispenseAtMs=startDateMs+currentIteration*intervalMs;
		if (schedState==SchedState.ACTIVE) {
			generateEscalations();
			cleanEscalations(nowMs);
		}
		return true;
	}
	
	private boolean handleWaitState(long nowMs) throws InterruptedException {
		if (nowMs>=dispenseAtMs-wakeBeforeMs) {
			schedState=SchedState.ACTIVE;
			currentIteration++;
			generateEscalations(); // WAIT->ACTIVE transition
			long nextMs=msToEscalation.isEmpty()?dispenseAtMs-wakeBeforeMs : msToEscalation.firstKey();
			long ms=nextMs-nowMs;
			innerWait(ms);
		} else {
			long ms=dispenseAtMs-wakeBeforeMs-nowMs;
			innerWait(ms);
		}
		return true;
	}
	
	private boolean handleActiveState(long nowMs) throws InterruptedException {
		// We might have reentered here, because we were interrupted by user getting the meds
		if (userPressedButton) {
			// User got the meds, success, reset, and wait till next iteration
			userPressedButton=false; // reset meds status
			dispenseAtMs+=intervalMs; // set timer to the next dispense
			schedState=SchedState.WAIT;
			long ms=dispenseAtMs-wakeBeforeMs-nowMs;
			innerWait(ms);
		} else {
			// user still didnt get the meds
			// cleanup/signal past-due escalations
			for (Iterator<Entry<Long, Escalation>> it = msToEscalation.entrySet().iterator(); it.hasNext();) {
				Entry<Long, Escalation> entry = it.next();
				long atMs=entry.getKey();
				Escalation escalation=entry.getValue();
				if (atMs>nowMs) break;
				if (atMs<=nowMs) {
					it.remove();
					signalEscalation(escalation);
				}
			}
			if (nowMs >= dispenseAtMs+waitAfterMs) {
				// out of active phase, user didnt get the meds
				schedState=SchedState.WAIT;
				currentIteration++;
				dispenseAtMs+=intervalMs; // set timer to the next dispense
				long ms=dispenseAtMs-wakeBeforeMs-nowMs;
				innerWait(ms);
				return true;
			}
			long nextWakeupMs = msToEscalation.isEmpty() ? dispenseAtMs+waitAfterMs :  msToEscalation.firstKey();
			long ms=nextWakeupMs-nowMs;
			innerWait(ms);
			// Do we still
		}
		return true;
	}
	
	private void innerWait(long ms) throws InterruptedException {
		if (ms>0) {
			System.out.println("Log: waiting "+ms+" ms. in "+schedState+" state,");
			wait(ms);
		}
	}
	
	synchronized boolean step() throws Exception {
		System.out.println("Log: ENTER step with state: "+schedState+" state, dispense: "+new Date(dispenseAtMs)+", currentIteration: "+currentIteration);
		try {
			long nowMs=System.currentTimeMillis();
			if (schedState==SchedState.INIT) {
				return handleInitState(nowMs);
			} else if (schedState==SchedState.COMPLETED) {
				return false;
			} else if (schedState==SchedState.WAIT) {
				return handleWaitState(nowMs);
			} else if (schedState==SchedState.ACTIVE) {
				return handleActiveState(nowMs);
			} else {
				throw new RuntimeException("Invalid state"+ schedState); 
			}
		} finally {
			System.out.println("Log: LEAVE step with state: "+schedState+" state, dispense: "+new Date(dispenseAtMs)+", currentIteration: "+currentIteration);	
		}
	}


	private void signalEscalation(Escalation e) {
		Brain.getBrain().say(e.message);
		System.out.println("Escalation! type: "+e.type+"; message: "+e.message);
		
	}

}
