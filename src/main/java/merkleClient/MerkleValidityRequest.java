package merkleClient;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static merkleClient.HashUtil.md5Java;

public class MerkleValidityRequest {

	/**
	 * IP address of the authority
	 * */
	private final String authIPAddr;
	/**
	 * Port number of the authority
	 * */
	private final int  authPort;
	/**
	 * Hash value of the merkle tree root. 
	 * Known before-hand.
	 * */
	private final String mRoot;
	/**
	 * List of transactions this client wants to verify 
	 * the existence of.
	 * */
	private List<String> mRequests;

	/**
	 * Sole constructor of this class - marked private.
	 * */
	private MerkleValidityRequest(Builder b){
		this.authIPAddr = b.authIPAddr;
		this.authPort = b.authPort;
		this.mRoot = b.mRoot;
		this.mRequests = b.mRequest;
	}
	
	/**
	 * <p>Method implementing the communication protocol between the client and the authority.</p>
	 * <p>The steps involved are as follows:</p>
	 * 		<p>0. Opens a connection with the authority</p>
	 * 	<p>For each transaction the client does the following:</p>
	 * 		<p>1.: asks for a validityProof for the current transaction</p>
	 * 		<p>2.: listens for a list of hashes which constitute the merkle nodes contents</p>
	 * 	<p>Uses the utility method {@link #isTransactionValid(String, List<String>) isTransactionValid} </p>
	 * 	<p>method to check whether the current transaction is valid or not.</p>
	 *
	 * 	<p>Addition to original function:</p>
	 *
	 * 	<p>Uses the utility method {@link #getNodesFromServer(Socket, String) getNodesFromServer}</p>
	 * 	<p>method to request and read the node hashes from the server.</p>
	 * */
	public Map<Boolean, List<String>> checkWhichTransactionValid() throws IOException {

		Map<Boolean, List<String>> checkedTransactions = new HashMap<>();
		checkedTransactions.put(true, new ArrayList<>());
		checkedTransactions.put(false, new ArrayList<>());
		/*
			Open connection to server
		 */
		try (Socket authSocket = new Socket(authIPAddr, authPort)){
			 mRequests.stream()
					.forEach(request -> {
						/*
						 * !!! Lambdas should be short and easy to read: hence why I moved the communication protocol
						 * into the (properly documented) dedicated function getNodesFromServer !!!
						 */
						List<String> mNodes = getNodesFromServer(authSocket, request);
						boolean validityProof = isTransactionValid(request, mNodes);
						checkedTransactions.get(validityProof).add(request);
					});
		} catch (IOException e) {
			System.out.println("Connection to server failed.\n" +
								"Aborting operation...\n");
		}
		return checkedTransactions;
	}

	/**
	 * 	Handles communication between client and server.
	 *
	 *  @param authSocket Socket: the socket from which the client communicates with the server
	 *  @param request String: hash code of the transaction we ask to validate
	 *
	 *  @return: list of node hashes received from the server
	 * */
	private List<String> getNodesFromServer (Socket authSocket, String request) {
		List<String> receivedNodes = new ArrayList<>();
		/*
			Ask server to provide the necessary node hashes to validate the transaction
		 */
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(authSocket.getOutputStream()),true)){
			String validityReqMsg = "Requesting node hashes to validate transaction...\n";
			out.write(validityReqMsg);
			out.write(request);
			/*
				Attempt to read node hashes from server
			*/
			try (BufferedReader in = new BufferedReader(new InputStreamReader(authSocket.getInputStream()))) {
				String nodeHash = in.readLine();
				while (nodeHash != null && !(nodeHash.equals("done"))) {
					receivedNodes.add(nodeHash);
					nodeHash = in.readLine();
				}
			} catch (IOException e) {
				System.out.println("Unable to read input stream from the server.\n");
			}
		} catch (IOException e) {
			System.out.println("Validation request to server failed.\n");
		}
		return receivedNodes;
	}

	/**
	 * 	Checks whether a transaction 'merkleTx' is part of the merkle tree.
	 *
	 *  @param merkleTx String: the transaction we want to validate
	 *  @param merkleNodes String: the hash codes of the merkle nodes required to compute
	 *  the merkle root
	 *  
	 *  @return: boolean value indicating whether this transaction was validated or not.
	 * */
	private boolean isTransactionValid(String merkleTx, List<String> merkleNodes) {
		/*
			Compute the root of the Merkle tree to check if the transaction is valid.
			I opted to use an atomic array because external variables in lambdas should be final!
		 */
		String[] computedRoot = new String[1];
		computedRoot[0] = merkleTx;
		merkleNodes.stream()
				.forEach(node -> {
					/*
						I assume that the server stores values in the Merkle tree such that every left child's hash is
						an even number, and every right child's hash is an odd number.
						In alternative, I could suppose a parent's child node hashes to be identical, so that I can
						just disregard the concatenation order when computing the tree root.
					 */
					short hashToInt = Short.parseShort(node.substring(node.length()-2, node.length()-1),16);
					if (hashToInt % 2 == 0)
						computedRoot[0] = md5Java(node+computedRoot[0]);
					else
						computedRoot[0] = md5Java(computedRoot[0]+node);
				});
		return  mRoot.equals(computedRoot[0]);
	}

	/**
	 * Builder for the MerkleValidityRequest class. 
	 * */
	public static class Builder {
		private String authIPAddr;
		private int authPort;
		private String mRoot;
		private List<String> mRequest;	
		
		public Builder(String authorityIPAddr, int authorityPort, String merkleRoot) {
			this.authIPAddr = authorityIPAddr;
			this.authPort = authorityPort;
			this.mRoot = merkleRoot;
			mRequest = new ArrayList<>();
		}
				
		public Builder addMerkleValidityCheck(String merkleHash) {
			mRequest.add(merkleHash);
			return this;
		}
		
		public MerkleValidityRequest build() {
			return new MerkleValidityRequest(this);
		}
	}
}
