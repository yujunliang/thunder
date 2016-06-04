package network.thunder.core.database;

import network.thunder.core.communication.NodeKey;
import network.thunder.core.communication.layer.DIRECTION;
import network.thunder.core.communication.layer.MessageWrapper;
import network.thunder.core.communication.layer.high.*;
import network.thunder.core.communication.layer.high.channel.ChannelSignatures;
import network.thunder.core.communication.layer.high.payments.PaymentData;
import network.thunder.core.communication.layer.high.payments.PaymentSecret;
import network.thunder.core.communication.layer.high.payments.messages.ChannelUpdate;
import network.thunder.core.communication.layer.middle.broadcasting.types.ChannelStatusObject;
import network.thunder.core.communication.layer.middle.broadcasting.types.P2PDataObject;
import network.thunder.core.communication.layer.middle.broadcasting.types.PubkeyIPObject;
import network.thunder.core.database.objects.PaymentWrapper;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.TransactionSignature;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Jean-Pierre Rupp on 04/06/16.
 */

public class HibernateDBHandler implements DBHandler {
    @Entity
    @Table(name = "channels")
    class ChannelEntity {
        private int id;
        private NodeKey nodeKeyClient;
        private ECKey keyClient;
        private ECKey keyServer;
        private byte[] masterPrivateKeyClient;
        private byte[] masterPrivateKeyServer;
        private int shaChainDepthCurrent;
        private int timestampOpen;
        private int timestampForceClose;
        private Transaction anchorTx;
        private int minConfirmationAnchor;
        private ChannelStatus channelStatus;
        private Channel.Phase phase;
        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ChannelSignatureEntity> channelSignatures = new ArrayList<>();
        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        private List<PaymentSignatureEntity> paymentSignatures = new ArrayList<>();
        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        private List<TransactionSignatureEntity> closingSignatures = new ArrayList<>();

        public Channel toChannel() {
            List<TransactionSignature> channelTransactionSignatures = this.channelSignatures.stream()
                    .map(ChannelSignatureEntity::getTransactionSignature)
                    .collect(Collectors.toList());
            List<TransactionSignature> paymentTransactionSignatures = this.paymentSignatures.stream()
                    .map(PaymentSignatureEntity::getTransactionSignature)
                    .collect(Collectors.toList());
            ChannelSignatures channelSignatures =
                    new ChannelSignatures(channelTransactionSignatures, paymentTransactionSignatures);
            Channel channel = new Channel();
            channel.id = id;
            channel.nodeKeyClient = nodeKeyClient;
            channel.keyClient = keyClient;
            channel.keyServer = keyServer;
            channel.masterPrivateKeyClient = masterPrivateKeyClient;
            channel.masterPrivateKeyServer = masterPrivateKeyServer;
            channel.shaChainDepthCurrent = shaChainDepthCurrent;
            channel.timestampOpen = timestampOpen;
            channel.timestampForceClose = timestampForceClose;
            channel.anchorTxHash = anchorTx.getHash();
            channel.anchorTx = anchorTx;
            channel.minConfirmationAnchor = minConfirmationAnchor;
            channel.channelStatus = channelStatus;
            channel.channelSignatures = channelSignatures;
            channel.phase = phase;
            channel.closingSignatures = closingSignatures.stream()
                    .map(TransactionSignatureEntity::getTransactionSignature)
                    .collect(Collectors.toList());
            return channel;
        }

        public ChannelEntity() {
        }

        public ChannelEntity(Channel channel) {
            id = channel.id;
            nodeKeyClient = channel.nodeKeyClient;
            keyClient = channel.keyClient;
            keyServer = channel.keyServer;
            masterPrivateKeyClient = channel.masterPrivateKeyClient;
            masterPrivateKeyServer = channel.masterPrivateKeyServer;
            shaChainDepthCurrent = channel.shaChainDepthCurrent;
            timestampOpen = channel.timestampOpen;
            timestampForceClose = channel.timestampForceClose;
            anchorTx = channel.anchorTx;
            minConfirmationAnchor = channel.minConfirmationAnchor;
            channelStatus = channel.channelStatus;
            channelSignatures = channel.channelSignatures.channelSignatures.stream()
                    .map(ChannelSignatureEntity::new)
                    .collect(Collectors.toList());
            paymentSignatures = channel.channelSignatures.paymentSignatures.stream()
                    .map(PaymentSignatureEntity::new)
                    .collect(Collectors.toList());
            phase = channel.phase;
            closingSignatures = channel.closingSignatures.stream()
                    .map(TransactionSignatureEntity::new)
                    .collect(Collectors.toList());
        }

        @Id
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public NodeKey getNodeKeyClient() {
            return nodeKeyClient;
        }

        public void setNodeKeyClient(NodeKey nodeKeyClient) {
            this.nodeKeyClient = nodeKeyClient;
        }

        public ECKey getKeyClient() {
            return keyClient;
        }

        public void setKeyClient(ECKey keyClient) {
            this.keyClient = keyClient;
        }

        public ECKey getKeyServer() {
            return keyServer;
        }

        public void setKeyServer(ECKey keyServer) {
            this.keyServer = keyServer;
        }

        public byte[] getMasterPrivateKeyClient() {
            return masterPrivateKeyClient;
        }

        public void setMasterPrivateKeyClient(byte[] masterPrivateKeyClient) {
            this.masterPrivateKeyClient = masterPrivateKeyClient;
        }

        public byte[] getMasterPrivateKeyServer() {
            return masterPrivateKeyServer;
        }

        public void setMasterPrivateKeyServer(byte[] masterPrivateKeyServer) {
            this.masterPrivateKeyServer = masterPrivateKeyServer;
        }

        public int getShaChainDepthCurrent() {
            return shaChainDepthCurrent;
        }

        public void setShaChainDepthCurrent(int shaChainDepthCurrent) {
            this.shaChainDepthCurrent = shaChainDepthCurrent;
        }

        public int getTimestampOpen() {
            return timestampOpen;
        }

        public void setTimestampOpen(int timestampOpen) {
            this.timestampOpen = timestampOpen;
        }

        public int getTimestampForceClose() {
            return timestampForceClose;
        }

        public void setTimestampForceClose(int timestampForceClose) {
            this.timestampForceClose = timestampForceClose;
        }

        public Transaction getAnchorTx() {
            return anchorTx;
        }

        public void setAnchorTx(Transaction anchorTx) {
            this.anchorTx = anchorTx;
        }

        public int getMinConfirmationAnchor() {
            return minConfirmationAnchor;
        }

        public void setMinConfirmationAnchor(int minConfirmationAnchor) {
            this.minConfirmationAnchor = minConfirmationAnchor;
        }

        public ChannelStatus getChannelStatus() {
            return channelStatus;
        }

        public void setChannelStatus(ChannelStatus channelStatus) {
            this.channelStatus = channelStatus;
        }

        public List<PaymentSignatureEntity> getPaymentSignatures() {
            return paymentSignatures;
        }

        public void setPaymentSignatures(List<PaymentSignatureEntity> paymentSignatures) {
            this.paymentSignatures = paymentSignatures;
        }

        public List<ChannelSignatureEntity> getChannelSignatures() {
            return channelSignatures;
        }

        public void setChannelSignatures(List<ChannelSignatureEntity> channelSignatures) {
            this.channelSignatures = channelSignatures;
        }

        public Channel.Phase getPhase() {
            return phase;
        }

        public void setPhase(Channel.Phase phase) {
            this.phase = phase;
        }

        public List<TransactionSignatureEntity> getClosingSignatures() {
            return closingSignatures;
        }

        public void setClosingSignatures(List<TransactionSignatureEntity> closingSignatures) {
            this.closingSignatures = closingSignatures;
        }
    }

    @Entity
    @Table(name = "transaction_signatures")
    class TransactionSignatureEntity {
        private TransactionSignature transactionSignature;
        private int id;

        public TransactionSignatureEntity() {
        }

        public TransactionSignatureEntity(TransactionSignature transactionSignature) {
            this.transactionSignature = transactionSignature;
        }

        @Id
        @GeneratedValue
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public TransactionSignature getTransactionSignature() {
            return transactionSignature;
        }

        public void setTransactionSignature(TransactionSignature transactionSignature) {
            this.transactionSignature = transactionSignature;
        }
    }

    @Entity
    @Table(name = "payment_signatures")
    class PaymentSignatureEntity {
        private TransactionSignature transactionSignature;
        private int id;

        public PaymentSignatureEntity() {
        }

        public PaymentSignatureEntity(TransactionSignature transactionSignature) {
            this.transactionSignature = transactionSignature;
        }

        @Id
        @GeneratedValue
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public TransactionSignature getTransactionSignature() {
            return transactionSignature;
        }

        public void setTransactionSignature(TransactionSignature transactionSignature) {
            this.transactionSignature = transactionSignature;
        }
    }

    @Entity
    @Table(name = "channel_signatures")
    class ChannelSignatureEntity {
        private TransactionSignature transactionSignature;
        private int id;

        public ChannelSignatureEntity() {
        }

        public ChannelSignatureEntity(TransactionSignature transactionSignature) {
            this.transactionSignature = transactionSignature;
        }

        @Id
        @GeneratedValue
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public TransactionSignature getTransactionSignature() {
            return transactionSignature;
        }

        public void setTransactionSignature(TransactionSignature transactionSignature) {
            this.transactionSignature = transactionSignature;
        }
    }

    @Override
    public List<MessageWrapper> getMessageList(NodeKey nodeKey, Sha256Hash channelHash, Class c) {
        return null;
    }

    @Override
    public List<AckableMessage> getUnackedMessageList(NodeKey nodeKey) {
        return null;
    }

    @Override
    public NumberedMessage getMessageResponse(NodeKey nodeKey, long messageIdReceived) {
        return null;
    }

    @Override
    public void setMessageAcked(NodeKey nodeKey, long messageId) {

    }

    @Override
    public void setMessageProcessed(NodeKey nodeKey, NumberedMessage message) {

    }

    @Override
    public long lastProcessedMessaged(NodeKey nodeKey) {
        return 0;
    }

    @Override
    public long saveMessage(NodeKey nodeKey, NumberedMessage message, DIRECTION direction) {
        return 0;
    }

    @Override
    public void linkResponse(NodeKey nodeKey, long messageRequest, long messageResponse) {

    }

    @Override
    public List<P2PDataObject> getSyncDataByFragmentIndex(int fragmentIndex) {
        return null;
    }

    @Override
    public List<P2PDataObject> getSyncDataIPObjects() {
        return null;
    }

    @Override
    public void insertIPObjects(List<P2PDataObject> ipList) {

    }

    @Override
    public List<PubkeyIPObject> getIPObjects() {
        return null;
    }

    @Override
    public P2PDataObject getP2PDataObjectByHash(byte[] hash) {
        return null;
    }

    @Override
    public PubkeyIPObject getIPObject(byte[] nodeKey) {
        return null;
    }

    @Override
    public void invalidateP2PObject(P2PDataObject ipObject) {

    }

    @Override
    public void syncDatalist(List<P2PDataObject> dataList) {

    }

    @Override
    public Channel getChannel(int id) {
        return null;
    }

    @Override
    public Channel getChannel(Sha256Hash hash) {
        return null;
    }

    @Override
    public List<Channel> getChannel(NodeKey nodeKey) {
        return null;
    }

    @Override
    public List<Channel> getOpenChannel(NodeKey nodeKey) {
        return null;
    }

    @Override
    public List<Channel> getOpenChannel() {
        return null;
    }

    @Override
    public void insertChannel(Channel channel) {

    }

    @Override
    public void updateChannelStatus (@NotNull NodeKey nodeKey, @NotNull Sha256Hash channelHash, @NotNull ECKey keyServer, Channel channel, ChannelUpdate
            update, List<RevocationHash> revocationHash, NumberedMessage request, NumberedMessage response) {

    }

    @Override
    public List<PubkeyIPObject> getIPObjectsWithActiveChannel() {
        return null;
    }

    @Override
    public List<ChannelStatusObject> getTopology() {
        return null;
    }

    @Override
    public List<PaymentData> lockPaymentsToBeRefunded(NodeKey nodeKey) {
        return null;
    }

    @Override
    public List<PaymentData> lockPaymentsToBeMade(NodeKey nodeKey) {
        return null;
    }

    @Override
    public List<PaymentData> lockPaymentsToBeRedeemed(NodeKey nodeKey) {
        return null;
    }

    @Override
    public void checkPaymentsList() {

    }

    @Override
    public void unlockPayments(NodeKey nodeKey, List<PaymentData> paymentList) {

    }

    @Override
    public NodeKey getSenderOfPayment(PaymentSecret paymentSecret) {
        return null;
    }

    @Override
    public void addPayment(NodeKey firstHop, PaymentData paymentWrapper) {

    }

    @Override
    public void updatePayment(PaymentWrapper paymentWrapper) {

    }

    @Override
    public PaymentWrapper getPayment(PaymentSecret paymentSecret) {
        return null;
    }

    @Override
    public PaymentSecret getPaymentSecret(PaymentSecret secret) {
        return null;
    }

    @Override
    public void addPaymentSecret(PaymentSecret secret) {

    }

    @Override
    public List<PaymentWrapper> getAllPayments() {
        return null;
    }

    @Override
    public List<PaymentWrapper> getOpenPayments() {
        return null;
    }

    @Override
    public List<PaymentWrapper> getRefundedPayments() {
        return null;
    }

    @Override
    public List<PaymentWrapper> getRedeemedPayments() {
        return null;
    }
}
