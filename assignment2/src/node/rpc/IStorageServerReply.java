package node.rpc;

public interface IStorageServerReply 
{
	String reply_createFile(int replyId, int sender, int result, String reply);
	String reply_getFile(int replyId, int sender, int result, String reply);
	String reply_putFile(int replyId, int sender, int result, String reply);
	String reply_appendToFile(int replyId, int sender, int result, String reply);
	String reply_deleteFile(int replyId, int sender, int result, String reply);
}
