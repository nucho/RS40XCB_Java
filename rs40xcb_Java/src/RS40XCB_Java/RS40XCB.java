package RS40XCB_Java;
// author: atsushi egashira
// license: LGPLv3

import java.io.IOException;

import Serial.Serial;
import Serial.SerialException;

public class RS40XCB {

	private static Serial serial;
	private byte[] sendbuf = new byte[32];
	private byte[] readbuf = new byte[32];

//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}

	/**
	 *  @param portname �V���A���|�[�g��
	 *  @param baudrate �ʐM���x
	 */
	public RS40XCB(String portname,int baudrate) {
		try {
			serial = new Serial(portname, baudrate, 'N', 8, 1);
			serial.clear();
		} catch (SerialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *  �T�[�{���w��p�x�֓�����
	 *  ���͈͂͒�����0�x�ŁC�T�[�{��ʂ��猩�Ď��v��肪+�C�����v��肪-
	 *  �w��p�x�̒P�ʂ�0.1�x�P��
	 *  �w�莞�Ԃ̒P�ʂ�10ms�P��(�ő�0.5%�̌덷)
	 *  
	 *  @param sId �T�[�{ID
	 *  @param sPos �w��p�x
	 *  @param sTime �w�莞��
	 */
	public void move(int sId, int sPos, int sTime) {
		byte sum;

		// �p�P�b�g�쐬
		sendbuf[0] = (byte) 0xFA; // �w�b�_�[1
		sendbuf[1] = (byte) 0xAF; // �w�b�_�[2
		sendbuf[2] = (byte) sId; // �T�[�{ID
		sendbuf[3] = (byte) 0x00; // �t���O
		sendbuf[4] = (byte) 0x1E; // �A�h���X(0x1E=30)
		sendbuf[5] = (byte) 0x04; // ����(4byte)
		sendbuf[6] = (byte) 0x01; // ��
		sendbuf[7] = (byte) (sPos & 0x00FF); // �ʒu
		sendbuf[8] = (byte) ((sPos & 0xFF00) >> 8); // �ʒu
		sendbuf[9] = (byte) (sTime & 0x00FF); // ����
		sendbuf[10] = (byte) ((sTime & 0xFF00) >> 8); // ����
		// �`�F�b�N�T���̌v�Z
		sum = sendbuf[2];
		for (int i = 3; i < 11; i++) {
			sum = (byte) (sum ^ sendbuf[i]);
		}
		sendbuf[11] = sum; // �`�F�b�N�T��

		// ���M
		// serialport.out.write(sendbuf, 0, 12);
		for(int i=0;i<12;i++)
			serial.write(sendbuf[i]);
	}

	/**
	 *  �T�[�{�̃g���N��ON/OFF�ł���
	 *  
	 *  @param sId �T�[�{ID
	 *  @param sMode ON/OFF�t���O true�Ńg���NON
	 */
	public void torque(int sId, boolean sMode) {
		byte sum;

		// �p�P�b�g�쐬
		sendbuf[0] = (byte) (0xFA); // �w�b�_�[1
		sendbuf[1] = (byte) (0xAF); // �w�b�_�[2
		sendbuf[2] = (byte) (sId); // �T�[�{ID
		sendbuf[3] = (byte) (0x00); // �t���O
		sendbuf[4] = (byte) (0x24); // �A�h���X(0x24=36)
		sendbuf[5] = (byte) (0x01); // ����(4byte)
		sendbuf[6] = (byte) (0x01); // ��
		if (sMode)
			sendbuf[7] = (byte) (0x001); // ON/OFF�t���O
		else
			sendbuf[7] = (byte) (0x0000);
		// �`�F�b�N�T���̌v�Z
		sum = sendbuf[2];
		for (int i = 3; i < 8; i++) {
			sum = (byte) (sum ^ sendbuf[i]);
		}
		sendbuf[8] = sum; // �`�F�b�N�T��

		// ���M
		for(int i=0;i<9;i++)
			serial.write(sendbuf[i]);
	}

	/**
	 *  �T�[�{�̌��݊p�x��0.1�x�P�ʂœ���
	 *  ���͈͂̒�����0�Ƃ��Ĕ����v����-150�x�C���v����150�x�͈̔�
	 *  
	 *  @param sId �T�[�{ID
	 *  @return ���݊p�x
	 */
	public int getAngle(int sId) {
		this.getParam(sId);
		return ((readbuf[8] << 8) & 0x0000FF00) | (readbuf[7] & 0x000000FF);
	}
	
	/**
	 *  �T�[�{���w�߂���M���C�ړ����J�n���Ă���̌o�ߎ��Ԃ�10ms�̒P�ʂœ���
	 *  �ړ�����������ƍŌ�̎��Ԃ�ێ�����
	 *  
	 *  @param sId �T�[�{ID
	 *  @return �o�ߎ���
	 */
	public int getTime(int sId) {
		this.getParam(sId);
		return ((readbuf[10] << 8) & 0x0000FF00) | (readbuf[9] & 0x000000FF);
	}
	
	/**
	 *  �T�[�{�̌��݉�]�X�s�[�h��deg/sec�P�ʂœ���
	 *  �u�Ԃ̃X�s�[�h������킵�Ă���
	 *  
	 *  @param sId �T�[�{ID
	 *  @return ���݉�]�X�s�[�h
	 */
	public int getSpeed(int sId) {
		this.getParam(sId);
		return ((readbuf[12] << 8) & 0x0000FF00) | (readbuf[11] & 0x000000FF);
	}
	
	/**
	 *  �T�[�{�̕��ׂ�mA�P�ʂœ���
	 *  
	 *  @param sId �T�[�{ID
	 *  @return ���ݕ���
	 */
	public int getLoad(int sId) {
		this.getParam(sId);
		return ((readbuf[14] << 8) & 0x0000FF00) | (readbuf[13] & 0x000000FF);
	}
	
	/**
	 *  �T�[�{�̊��̉��x�𓾂�
	 *  ���x�Z���T�ɂ͌̍��ɂ�� +-3�x���x�̌덷������
	 *  ��x���x�ɂ��ی�@�\�������ƁC�T�[�{�����Z�b�g����K�v������
	 *  
	 *  @param sId �T�[�{ID
	 *  @return ���݉��x
	 */
	public int getTemperature(int sId) {
		this.getParam(sId);
		return ((readbuf[16] << 8) & 0x0000FF00) | (readbuf[15] & 0x000000FF);
	}
	
	/**
	 *  �T�[�{�ɋ�������Ă���d���̓d����10mV�P�ʂœ���
	 *  ���悻+-0.3V���x�̌덷������
	 *  
	 *  @param sId �T�[�{ID
	 *  @return �d���d��
	 */
	public int getVoltage(int sId) {
		this.getParam(sId);
		return ((readbuf[18] << 8) & 0x0000FF00) | (readbuf[17] & 0x000000FF);
	}
	

	
	private void getParam(int sId)
	{
		byte sum;

		// �p�P�b�g�쐬
		sendbuf[0] = (byte) 0xFA; // �w�b�_�[1
		sendbuf[1] = (byte) 0xAF; // �w�b�_�[2
		sendbuf[2] = (byte) sId; // �T�[�{ID
		sendbuf[3] = (byte) 0x09; // �t���O(0x01 | 0x04<<1)
		sendbuf[4] = (byte) 0x00; // �A�h���X(0x00)
		sendbuf[5] = (byte) 0x00; // ����(0byte)
		sendbuf[6] = (byte) 0x01; // ��
		// �`�F�b�N�T���̌v�Z
		sum = sendbuf[2];
		for (int i = 3; i < 7; i++) {
			sum = (byte) (sum ^ sendbuf[i]);
		}
		sendbuf[7] = sum; // �`�F�b�N�T��

		// ���M
		for(int i=0;i<8;i++)
			serial.write(sendbuf[i]);

		// ��M�̂��߂ɏ����҂�
		for (int i = 0; i < 10; i++) {
			if (serial.available() >= 26)
				break;

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// �ǂݍ���
		for(int i=0;i<26;i++)
			readbuf[i] = (byte)(serial.read() & 0xff);

//		if (len < 26) {
//			// ��M�G���[
//			System.out.println("��M�G���[");
//			// return -2;
//		}

		// ��M�f�[�^�̊m�F
//		sum = readbuf[2];
//		for (i = 3; i < 26; i++) {
//			sum = sum ^ readbuf[i];
//		}
//		if (sum) {
//			// �`�F�b�N�T���G���[
//			return -3;
//		}

	}

}
