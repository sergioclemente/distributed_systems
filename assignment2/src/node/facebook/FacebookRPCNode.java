package node.facebook;

import java.util.Vector;

import node.rpc.RPCMethodCall;
import node.rpc.RPCNode;
import node.rpc.I2pcParticipant;
import node.twophasecommit.TwoPhaseCommitNode;

public class FacebookRPCNode extends RPCNode {
	private FacebookShardSystem m_shard;
	private FacebookFrontendSystem m_frontEnd;
	private TwoPhaseCommitNode m_twoPhase;

	private static Vector<Integer> s_shardAddresses = new Vector<Integer>();

	public static final String ERROR_MESSAGE_FORMAT = "Error: %s on facebook server %d. Returned error code was %s";

	public FacebookRPCNode() {
		super();
	}

	@Override
	public void start() {
		super.start();

		if (this.addr == 0) {
			this.user_info("Starting frontend instance on address " + this.addr);
			this.m_frontEnd = new FacebookFrontendSystem(this);
		} else {
			this.user_info("Starting sharding instance on address " + this.addr);
			this.m_shard = new FacebookShardSystem(this);
			s_shardAddresses.add(this.addr);
		}

		this.user_info("Starting 2PC coordinator support on address " + this.addr);
		this.m_twoPhase = new TwoPhaseCommitNode(this);

		// Enable this node to receive RPC calls for these interfaces
		this.bindFacebookServerImpl(m_shard);
		this.bind2pcCoordinatorImpl(m_twoPhase);
		this.bind2pcParticipantImpl((I2pcParticipant) m_twoPhase);

		if (this.m_shard != null) {
			this.m_shard.recoverFromCrash();
		}
	}
	
	public static Vector<Integer> getShardAddresses() {
		return s_shardAddresses;
	}

	@Override
	public void onCommand(String command) {
		boolean handled = false;

		// Let the facebook frontend take the command first
		// TODO: move this to a chain of responsibility
		if (this.m_frontEnd != null) {
			handled = m_frontEnd.onCommand(command);
		}

		if (!handled) {
			// See if the 2PC coordinator can handle this command
			handled = m_twoPhase.onCommand(command);
		}

		// TODO: queue logic not in place
	}

	@Override
	protected void onConnectionAborted(int endpoint) {
		this.user_info("connection aborted!");
	}

	private void user_info(String s) {
		// Use a different prefix to be easy to distinguish
		System.out.println(">>>> " + s);
	}
}
